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
package cz.cacek.test;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.Field;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.eclipse.aether.version.InvalidVersionSpecificationException;

/**
 * Collection of some use cases.
 */
@Singleton
@Named
public class LastModifiedIdx {
    private final Indexer indexer;

    private final IndexUpdater indexUpdater;

    private IndexingContext centralContext;

    @Inject
    public LastModifiedIdx(Indexer indexer, IndexUpdater indexUpdater) {
        this.indexer = requireNonNull(indexer);
        this.indexUpdater = requireNonNull(indexUpdater);
    }

    public void perform() throws IOException, InvalidVersionSpecificationException {
        // Files where local cache is (if any) and Lucene Index should be located
        File centralLocalCache = new File("central-cache-lm");
        File centralIndexDir = new File("central-index-lm");

        // Creators we want to use (search for fields it defines)
        List<IndexCreator> indexers = new ArrayList<>();
        indexers.add(new CustomArtifactInfoIndexCreator());

        // Create context for central repository index
        centralContext = indexer.createIndexingContext("central-context", "central", centralLocalCache, centralIndexDir,
                "https://repo1.maven.org/maven2", null, true, true, indexers);

        // Update the index (incremental update will happen if this is not 1st run and files are not deleted)
        // This whole block below should not be executed on every app start, but rather controlled by some configuration
        // since this block will always emit at least one HTTP GET. Central indexes are updated once a week, but
        // other index sources might have different index publishing frequency.
        // Preferred frequency is once a week.
        Instant updateStart = Instant.now();
        System.out.println("Updating Index - LastModifiedIdx...");
        System.out.println("This might take a while on first run, so please be patient!");

        Date centralContextCurrentTimestamp = centralContext.getTimestamp();
        IndexUpdateRequest updateRequest = new IndexUpdateRequest(centralContext, new Java11HttpClient());
        IndexUpdateResult updateResult = indexUpdater.fetchAndUpdateIndex(updateRequest);
        if (updateResult.isFullUpdate()) {
            System.out.println("Full update happened!");
        } else if (updateResult.getTimestamp().equals(centralContextCurrentTimestamp)) {
            System.out.println("No update needed, index is up to date!");
        } else {
            System.out.println("Incremental update happened, change covered " + centralContextCurrentTimestamp + " - "
                    + updateResult.getTimestamp() + " period.");
        }

        System.out.println("Finished in " + Duration.between(updateStart, Instant.now()).getSeconds() + " sec");
        System.out.println();

        System.out.println();
        System.out.println("Using index");
        System.out.println("===========");
        System.out.println();

        Instant start = Instant.now();
        // construct the filter
        long twoWeeksAgo = start.minus(14, ChronoUnit.DAYS).toEpochMilli();
//        Query query = LongPoint.newRangeQuery("m2", twoWeeksAgo, Long.MAX_VALUE);

        BooleanQuery query = new BooleanQuery.Builder()
                .add(indexer.constructQuery(MAVEN.PACKAGING, new SourcedSearchExpression("jar")), Occur.MUST)
                .add(indexer.constructQuery(MAVEN.CLASSIFIER, new SourcedSearchExpression(Field.NOT_PRESENT)), Occur.MUST_NOT)
                .add(LongPoint.newRangeQuery("m2", twoWeeksAgo, Long.MAX_VALUE), Occur.MUST)
                .build();


        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(query, centralContext));
        long count = 0L;
        for (ArtifactInfo ai : response.getResults()) {
            System.out.println(ai.toString());
            count++;
        }
        long secondsDiff = Duration.between(start, Instant.now()).getSeconds();
        System.out.println("------");
        System.out.println("Total response size: " + response.getTotalHitsCount());
        System.out.println("Count: " + count);
        System.out.println("Total artifacts changed in last 2 weeks: " + response.getTotalHitsCount());
        System.out.println("Search took " + secondsDiff + " seconds");
        System.out.println();

        // close cleanly
        indexer.closeIndexingContext(centralContext, false);
    }
    
    public void searchAndDump(Indexer nexusIndexer, String descr, Query q) throws IOException {
        System.out.println("Searching for " + descr);

        FlatSearchResponse response = nexusIndexer.searchFlat(new FlatSearchRequest(q, centralContext));

        for (ArtifactInfo ai : response.getResults()) {
            System.out.println(ai.toString());
        }

        System.out.println("------");
        System.out.println("Total: " + response.getTotalHitsCount());
        System.out.println();
    }
}