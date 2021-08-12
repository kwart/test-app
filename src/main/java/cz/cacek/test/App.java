package cz.cacek.test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.jet.pipeline.Sources;


import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.datamodel.ItemsByTag;
import com.hazelcast.jet.datamodel.Tag;
import com.hazelcast.jet.impl.util.Util;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.jet.pipeline.StageWithKeyAndWindow;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStageWithKey;
import com.hazelcast.jet.pipeline.WindowGroupAggregateBuilder;
import com.hazelcast.map.IMap;

import java.util.concurrent.locks.LockSupport;

import static com.hazelcast.jet.Util.mapEventNewValue;
import static com.hazelcast.jet.Util.mapPutEvents;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.jet.aggregate.AggregateOperations.toList;
import static com.hazelcast.jet.pipeline.JournalInitialPosition.START_FROM_OLDEST;
import static com.hazelcast.jet.pipeline.WindowDefinition.sliding;

public class App {

    public static void main(String[] args) {
        StreamSource<JsonObject> src = Sources.<JsonObject, Integer, JsonObject> mapJournal("logs", START_FROM_OLDEST, mapEventNewValue(), mapPutEvents());
        Pipeline.create().readFrom(src).withoutTimestamps().filter(j->{
            String strLevel = j.get("level").asString();
            return strLevel==null || "ERROR".equalsIgnoreCase(strLevel) || "WARNING".equalsIgnoreCase(strLevel);
        });
    }
}
