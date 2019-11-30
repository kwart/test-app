package a_map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppHashMap {

    public static void main(String[] args) {
        Map<String, Integer> cityInhabitants = new ConcurrentHashMap<>();

        cityInhabitants.put("Istanbul", 15_067_724);
        cityInhabitants.put("London", 9_126_366);
        cityInhabitants.put("Prague", 1_308_632);

        //...

        System.out.println("London population: " + cityInhabitants.get("London"));
    }
}
