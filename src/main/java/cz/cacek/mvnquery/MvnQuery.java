/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cz.cacek.mvnquery;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.LogManager;

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.Field;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.IteratorSearchRequest;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.context.ExistingLuceneIndexMismatchException;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.sisu.launch.Main;
import org.eclipse.sisu.space.BeanScanning;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.internal.DefaultConsole;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Query Maven Index. See
 * https://stackoverflow.com/questions/5776519/how-to-parse-unzip-unpack-maven-repository-indexes-generated-by-nexus
 */
public class MvnQuery {

    private final Indexer indexer;
    private final IndexUpdater indexUpdater;
    private final Config config;

    public MvnQuery(Config config) throws Exception {
        this.config = requireNonNull(config);
        Injector injector = Guice.createInjector(Main.wire(BeanScanning.CACHE));
        MvnIndexerContext ctx = injector.getInstance(MvnIndexerContext.class);
        this.indexer = ctx.indexer;
        this.indexUpdater = ctx.indexUpdater;
    }

    public static void main(String args[]) throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Config config = new Config();
        JCommander jcmd = JCommander.newBuilder().programName("MvnQuery").console(new DefaultConsole(System.err))
                .acceptUnknownOptions(true).addObject(config).build();
        jcmd.parse(args);
        if (config.isPrintHelp() || !jcmd.getUnknownOptions().isEmpty()) {
            jcmd.setUsageFormatter(new UsageFormatter(jcmd));
            jcmd.usage();
            System.exit(2);
        } else {
            MvnQuery app = new MvnQuery(config);
            app.perform();
        }
    }

    public void perform() throws IOException, InvalidVersionSpecificationException {
        IndexingContext indexingContext = initIndexingContext();
        updateIndex(indexingContext);
        BooleanQuery query = buildQuery();
        runQuery(indexingContext, query);
        indexer.closeIndexingContext(indexingContext, false);
    }

    private void runQuery(IndexingContext indexingContext, BooleanQuery query) throws IOException {
        log("Querying index");
        log("------");
        Instant searchStart = Instant.now();
        final IteratorSearchRequest request = new IteratorSearchRequest(query, Collections.singletonList(indexingContext));
        long count = 0L;
        try (final IteratorSearchResponse response = indexer.searchIterator(request)) {
            for (ArtifactInfo ai : response) {
                System.out.println(getCoordinates(ai));
                count++;
            }
            long secondsDiff = Duration.between(searchStart, Instant.now()).getSeconds();
            log("------");
            log("Total response size: " + response.getTotalHitsCount());
            log("Artifacts listed: " + count);
            log("Query took " + secondsDiff + " seconds");
            log();
        }
    }

    private String getCoordinates(ArtifactInfo ai) {
        return ai.getGroupId() + ":" + ai.getArtifactId() + ":" + ai.getVersion() + ":" + ai.getPackaging() + ":"
                + Objects.toString(ai.getClassifier(), "");
    }

    private BooleanQuery buildQuery() {
        log("Building the query");

        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        addToQuery(builder, MAVEN.GROUP_ID, config.getGroupId());
        addToQuery(builder, MAVEN.ARTIFACT_ID, config.getArtifactId());
        addToQuery(builder, MAVEN.PACKAGING, config.getPackaging());
        if (!addToQuery(builder, MAVEN.CLASSIFIER, config.getClassifier()) && config.getClassifier() != null) {
            builder.add(indexer.constructQuery(MAVEN.CLASSIFIER, new SourcedSearchExpression(Field.NOT_PRESENT)),
                    Occur.MUST_NOT);
        }

        int lastDays = config.getLastDays();
        if (lastDays > 0) {
            long lastModifiedRangeStart = Instant.now().minus(lastDays, ChronoUnit.DAYS).toEpochMilli();
            builder.add(LongPoint.newRangeQuery(CustomArtifactInfoIndexCreator.FLD_LAST_MODIFIED.getKey(),
                    lastModifiedRangeStart, Long.MAX_VALUE), Occur.MUST);
        }
        BooleanQuery query = builder.build();
        log("\t" + query);
        return query;
    }

    private boolean addToQuery(Builder builder, Field field, String val) {
        if (null == val || "-".equals(val)) {
            return false;
        }
        builder.add(indexer.constructQuery(field, new SourcedSearchExpression(val)), Occur.MUST);
        return true;
    }

    private void updateIndex(IndexingContext indexingContext) throws IOException {
        // Update the index (incremental update will happen if this is not 1st run and files are not deleted)
        // This whole block below should not be executed on every app start, but rather controlled by some configuration
        // since this block will always emit at least one HTTP GET. Maven Central indexes are updated once a week, but
        // other index sources might have different index publishing frequency.
        // Preferred frequency is once a week.
        Instant updateStart = Instant.now();
        log("Updating Index ...");
        log("\tThis might take a while on first run, so please be patient!");

        Date contextCurrentTimestamp = indexingContext.getTimestamp();
        IndexUpdateRequest updateRequest = new IndexUpdateRequest(indexingContext, new Java11HttpClient());
        IndexUpdateResult updateResult = indexUpdater.fetchAndUpdateIndex(updateRequest);
        if (updateResult.isFullUpdate()) {
            log("\tFull update happened!");
        } else {
            Date timestamp = updateResult.getTimestamp();
            if (timestamp == null || timestamp.equals(contextCurrentTimestamp)) {
                log("\tNo update needed, index is up to date!");
            } else {
                log("\tIncremental update happened, change covered " + contextCurrentTimestamp + " - " + timestamp
                        + " period.");
            }
        }
        log("\tFinished in " + Duration.between(updateStart, Instant.now()).getSeconds() + " sec");
        log();
    }

    private IndexingContext initIndexingContext() throws IOException, ExistingLuceneIndexMismatchException {
        String configRepo = config.getConfigRepo();
        log("Initiating indexing context for " + configRepo);
        String repoHash = hashRepo(configRepo);
        File repoDir = new File(config.getConfigDataDir(), repoHash);
        log("\t- repository index data location: " + repoDir);
        if (!repoDir.exists()) {
            log("\t- creating index data directory");
            repoDir.mkdirs();
        }

        Path repoUrlPath = repoDir.toPath().resolve("repo-url");
        if (!Files.exists(repoUrlPath)) {
            Files.writeString(repoUrlPath, configRepo, StandardOpenOption.CREATE);
        }
        File cacheDir = new File(repoDir, "cache");
        File indexDir = new File(repoDir, "index");

        // Use a custom creator which indexes lastModified ArtifactInfo value
        List<IndexCreator> indexers = new ArrayList<>();
        indexers.add(new CustomArtifactInfoIndexCreator());

        // Create context for central repository index
        return indexer.createIndexingContext(repoHash, repoHash, cacheDir, indexDir, configRepo, null, true, true, indexers);
    }

    private static String hashRepo(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 10);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private void log(String line) {
        if (!config.isQuiet()) {
            System.err.println(line);
        }
    }

    private void log() {
        log("");
    }
}