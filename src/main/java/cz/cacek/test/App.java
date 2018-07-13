package cz.cacek.test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.instance.HazelcastInstanceFactory;

/**
 * Hazelcast Hello world!
 */
public class App {

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.version"));
        Config config = new Config();
        
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.getInterfaces().addInterface("127.0.0.1");
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true).addMember("127.0.0.1");
        long time = System.currentTimeMillis();
        for (int i=0; i<20; i++) {
            try {
                Hazelcast.newHazelcastInstance(config);
                Hazelcast.newHazelcastInstance(config);
                HazelcastClient.newHazelcastClient();
            } finally {
                HazelcastClient.shutdownAll();
                HazelcastInstanceFactory.terminateAll();
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Total time(ms): " + time);
    }
}
