package c_set;

import java.util.Set;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CollectionConfiguration;

public class AppIgnite {

    public static void main(String[] args) {

        try (Ignite ignite = Ignition.start()) {

            Set<String> imdgs = ignite.set("imdgs", new CollectionConfiguration());

            imdgs.add("Hazelcast");
            imdgs.add("Infinispan");
            imdgs.add("Ignite");
            imdgs.add("Hazelcast");

            // ...

            System.out.println("Set size: " + imdgs.size());
        }
    }
}
