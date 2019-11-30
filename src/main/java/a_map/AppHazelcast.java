package a_map;

import java.util.Map;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class AppHazelcast {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        Map<String, Integer> cityInhabitants = hz.getMap("cityInhabitants");

        cityInhabitants.put("Istanbul", 15_067_724);
        cityInhabitants.put("London", 9_126_366);
        cityInhabitants.put("Prague", 1_308_632);

        //...

        System.out.println("London population: " + cityInhabitants.get("London"));

        hz.shutdown();
    }
}
