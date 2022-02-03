package cz.cacek.test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.map.IMap;

/**
 * The App!
 */
public class App {

    public static void main(String[] args) {
        System.setProperty("hazelcast.logging.type", "log4j2");
//        System.setProperty("hazelcast.enterprise.license.key", "");
        try {
            HazelcastInstance hz = Hazelcast.newHazelcastInstance();
            IMap<String, String> map = hz.getMap("test");
            map.put("key", "value");
            if ("value".equals(hz.getMap("key"))) {
                System.out.println("OK");
            }
        } finally {
            HazelcastInstanceFactory.terminateAll();
        }
    }
}
