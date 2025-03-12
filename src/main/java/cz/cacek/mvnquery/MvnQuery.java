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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

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

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Query Maven Index. 
 * See https://stackoverflow.com/questions/5776519/how-to-parse-unzip-unpack-maven-repository-indexes-generated-by-nexus
 */
public class MvnQuery {
    private final Indexer indexer;
    private final IndexUpdater indexUpdater;
//    private final Config config;

    private final static String QUERY_PREFIX = "query.";
    public final static String PROP_GROUP_ID = QUERY_PREFIX + "groupId";
    public final static String PROP_ARTIFACT_ID = QUERY_PREFIX + "artifactId";
    public final static String PROP_PACKAGING = QUERY_PREFIX + "packaging";
    public final static String PROP_CLASSIFIER = QUERY_PREFIX + "classifier";
    public final static String PROP_LAST_DAYS = QUERY_PREFIX + "lastDays";

    private final static String CONFIG_PREFIX = "config.";
    public final static String PROP_CFG_DATA_DIR = CONFIG_PREFIX + "dataDir";
    public final static String PROP_CFG_REPO = CONFIG_PREFIX + "repo";

    private final String QVAL_GROUP_ID = System.getProperty(PROP_GROUP_ID);
    private final String QVAL_ARTIFACT_ID = System.getProperty(PROP_ARTIFACT_ID);
    private final String QVAL_PACKAGING = System.getProperty(PROP_PACKAGING, "jar");
    private final String QVAL_CLASSIFIER = System.getProperty(PROP_CLASSIFIER, "-");
    private final int QVAL_LAST_DAYS = Integer.getInteger(PROP_LAST_DAYS, 14);

    private final File CFG_DATA_DIR = new File(
            System.getProperty(PROP_CFG_DATA_DIR, System.getProperty("user.home") + File.separator + ".mvnindex"));
    private final String CFG_REPO = System.getProperty(PROP_CFG_REPO, "https://repo1.maven.org/maven2");

    public MvnQuery() throws Exception {
        Injector injector = Guice.createInjector(Main.wire(BeanScanning.INDEX));
        this.indexer = injector.getInstance(Indexer.class);
        this.indexUpdater = injector.getInstance(IndexUpdater.class);
    }

    public static void main(String args[]) throws Exception {
        MvnQuery app = new MvnQuery();
        app.perform();
    }
    
    public void perform() throws IOException, InvalidVersionSpecificationException {
        IndexingContext indexingContext = initIndexingContext();
        updateIndex(indexingContext);
        BooleanQuery query = buildQuery();
        runQuery(indexingContext, query);
        indexer.closeIndexingContext(indexingContext, false);
    }

    private void runQuery(IndexingContext indexingContext, BooleanQuery query) throws IOException {
        System.err.println("Querying index");
        System.err.println("------");
        Instant searchStart = Instant.now();
        final IteratorSearchRequest request = new IteratorSearchRequest(query, Collections.singletonList(indexingContext));
        long count = 0L;
        try (final IteratorSearchResponse response = indexer.searchIterator(request)) {
            for (ArtifactInfo ai : response) {
                System.out.println(getCoordinates(ai));
                count++;
            }
            long secondsDiff = Duration.between(searchStart, Instant.now()).getSeconds();
            System.err.println("------");
            System.err.println("Total response size: " + response.getTotalHitsCount());
            System.err.println("Artifacts listed: " + count);
            System.err.println("Query took " + secondsDiff + " seconds");
            System.err.println();
        }
        // FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(query, indexingContext));
        // for (ArtifactInfo ai : response.getResults()) {
        // System.out.println(getCoordinates(ai));
        // }
    }

    private String getCoordinates(ArtifactInfo ai) {
        return ai.getGroupId() + ":" + ai.getArtifactId() + ":" + ai.getVersion() + ":" + ai.getPackaging() + ":"
                + Objects.toString(ai.getClassifier(), "");
    }

    private BooleanQuery buildQuery() {
        System.err.println("Building the query");

        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        addToQuery(builder, MAVEN.GROUP_ID, QVAL_GROUP_ID);
        addToQuery(builder, MAVEN.ARTIFACT_ID, QVAL_ARTIFACT_ID);
        addToQuery(builder, MAVEN.PACKAGING, QVAL_PACKAGING);
        if (!addToQuery(builder, MAVEN.CLASSIFIER, QVAL_CLASSIFIER) && QVAL_CLASSIFIER != null) {
            builder.add(indexer.constructQuery(MAVEN.CLASSIFIER, new SourcedSearchExpression(Field.NOT_PRESENT)),
                    Occur.MUST_NOT);
        }

        if (QVAL_LAST_DAYS > 0) {
            long lastModifiedRangeStart = Instant.now().minus(QVAL_LAST_DAYS, ChronoUnit.DAYS).toEpochMilli();
            builder.add(LongPoint.newRangeQuery(CustomArtifactInfoIndexCreator.FLD_LAST_MODIFIED.getKey(),
                    lastModifiedRangeStart, Long.MAX_VALUE), Occur.MUST);
        }
        BooleanQuery query = builder.build();
        System.err.println("\t" + query);
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
        System.err.println("Updating Index ...");
        System.err.println("\tThis might take a while on first run, so please be patient!");

        Date centralContextCurrentTimestamp = indexingContext.getTimestamp();
        IndexUpdateRequest updateRequest = new IndexUpdateRequest(indexingContext, new Java11HttpClient());
        IndexUpdateResult updateResult = indexUpdater.fetchAndUpdateIndex(updateRequest);
        if (updateResult.isFullUpdate()) {
            System.err.println("Full update happened!");
        } else if (updateResult.getTimestamp().equals(centralContextCurrentTimestamp)) {
            System.err.println("No update needed, index is up to date!");
        } else {
            System.err.println("Incremental update happened, change covered " + centralContextCurrentTimestamp + " - "
                    + updateResult.getTimestamp() + " period.");
        }
        System.err.println("Finished in " + Duration.between(updateStart, Instant.now()).getSeconds() + " sec");
        System.err.println();
    }

    private IndexingContext initIndexingContext() throws IOException, ExistingLuceneIndexMismatchException {
        System.err.println("Initiating indexing context for " + CFG_REPO);
        String repoHash = hashRepo(CFG_REPO);
        File repoDir = new File(CFG_DATA_DIR, repoHash);
        System.err.println("\t- repository index data location: " + repoDir);
        if (!repoDir.exists()) {
            System.err.println("\t- creating index data directory");
            repoDir.mkdirs();
        }

        Path repoUrlPath = repoDir.toPath().resolve("repo-url");
        if (!Files.exists(repoUrlPath)) {
            Files.writeString(repoUrlPath, CFG_REPO, StandardOpenOption.CREATE);
        }
        File cacheDir = new File(repoDir, "cache");
        File indexDir = new File(repoDir, "index");

        // Use a custom creator which indexes lastModified ArtifactInfo value
        List<IndexCreator> indexers = new ArrayList<>();
        indexers.add(new CustomArtifactInfoIndexCreator());

        // Create context for central repository index
        return indexer.createIndexingContext(repoHash, repoHash, cacheDir, indexDir, CFG_REPO, null, true, true, indexers);
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
    
    /**
     * Prints usage information for defined system properties.
     */
    public static void printUsage() {
        System.err.println("Usage: Configure the following system properties to modify behavior:");
        System.err.println();
        System.err.println("Query Parameters:");
        System.err.println("  -D" + PROP_GROUP_ID + "=<groupId>       (Filter by groupId, e.g., org.apache.maven)");
        System.err.println("  -D" + PROP_ARTIFACT_ID + "=<artifactId>   (Filter by artifactId, e.g., maven-core)");
        System.err.println("  -D" + PROP_PACKAGING + "=<packaging>     (Filter by packaging type, default: jar)");
        System.err.println("  -D" + PROP_CLASSIFIER + "=<classifier>   (Filter by classifier, default: \"-\")");
        System.err.println("  -D" + PROP_LAST_DAYS + "=<days>         (Filter artifacts modified in last X days, default: 14)");
        System.err.println();
        System.err.println("Configuration Parameters:");
        System.err.println("  -D" + PROP_CFG_DATA_DIR + "=<path>       (Set data directory for index, default: ~/.mvnindex)");
        System.err.println("  -D" + PROP_CFG_REPO + "=<URL>          (Set repository URL, default: https://repo1.maven.org/maven2)");
        System.err.println();
        System.err.println("Example usage:");
        System.err.println("  java -D" + PROP_GROUP_ID + "=org.apache.maven -D" + PROP_LAST_DAYS + "=30 -jar my-app.jar");
    }
}