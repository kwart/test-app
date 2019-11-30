package c_set;

import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class AppHazelcast {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        Set<String> imdgs = hz.getSet("imdgs");

        imdgs.add("Hazelcast");
        imdgs.add("Infinispan");
        imdgs.add("Ignite");
        imdgs.add("Hazelcast");
        
        //...

        System.out.println("Set size: " + imdgs.size());
        hz.shutdown();
    }
}
