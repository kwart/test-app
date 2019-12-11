package a_map;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

public class AppIgnite {

    public static void main(String[] args) {

        try (Ignite ignite = Ignition.start()) {

            // IgniteCache interface doesn't extend java.util.Map!
            IgniteCache<String, Integer> cityInhabitants = ignite.getOrCreateCache("cityInhabitants");

            if (cityInhabitants.size() == 0) {
                System.out.println("Initializing the cache");
                cityInhabitants.put("Istanbul", 15_067_724);
                cityInhabitants.put("London", 9_126_366);
                cityInhabitants.put("Prague", 1_308_632);
            } else {
                System.out.println("Cache is already filled");
            }

            // ...

            System.out.println("London population: " + cityInhabitants.get("London"));
        }
    }
}
