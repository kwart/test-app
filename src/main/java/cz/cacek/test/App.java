package cz.cacek.test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;

/**
 * The App!
 */
public class App {

    public static void main(String[] args) {
        System.setProperty("hazelcast.logging.type", "log4j2");
        try {
            HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        } finally {
            HazelcastInstanceFactory.terminateAll();
        }
    }
}
