package c_set;

import java.util.HashSet;
import java.util.Set;

public class AppHashSet {

    public static void main(String[] args) {
        Set<String> imdgs = new HashSet<>();

        imdgs.add("Hazelcast");
        imdgs.add("Infinispan");
        imdgs.add("Ignite");
        imdgs.add("Hazelcast");

        //...

        System.out.println("Set size: " + imdgs.size());
    }
}
