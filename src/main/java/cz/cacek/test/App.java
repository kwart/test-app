package cz.cacek.test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.instance.HazelcastInstanceFactory;

/**
 * Hazelcast Hello world!
 */
public class App {

    public static void main(String[] args) {
        try {
            Hazelcast.newHazelcastInstance();
        } finally {
            HazelcastInstanceFactory.terminateAll();
        }
    }
}
