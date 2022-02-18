package cz.cacek.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.partition.Partition;

/**
 * The App!
 */
public class App {

    public static void main(String[] args) throws Exception {
        List<String> members = new ArrayList<>();
        members.add("127.0.0.1");

        int port = 33536;
        Collection<Integer> ports = new ArrayList<>();
        for (int i = port + 2; i < port + 9; i++) {
            ports.add(i);
        }
        Config config = new Config();
        config.getNetworkConfig().setPortAutoIncrement(true);
        config.getNetworkConfig().setPort(port);
        config.getNetworkConfig().setOutboundPorts(ports);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).setMembers(members);

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        Map<Integer, Integer> map = hazelcastInstance.getMap("m");
        for (int i = 0; i < 2100; i++) {
            map.put(i, i);
        }

        boolean nodeRunning = !hazelcastInstance.getCluster().getMembers().isEmpty();
        int size = hazelcastInstance.getMap("m").size();
        int count = 0;
        while (nodeRunning && size > 0) {
            TimeUnit.MINUTES.sleep(3);
            nodeRunning = !hazelcastInstance.getCluster().getMembers().isEmpty();

            if (nodeRunning) {
                size = hazelcastInstance.getMap("m").size();
                System.out.printf("%5d nodeRunning=%s map size %d @ %s%n", count++, nodeRunning, size, new java.util.Date());
            } else {
                Partition partition = hazelcastInstance.getPartitionService().getPartition(1000);

                System.out.printf("Parition owner of key [%5d] is %s", 1000, partition.getOwner());
            }
        }
        hazelcastInstance.shutdown();
    }

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("com.hazelcast.cluster.impl");
    private static final java.util.logging.Logger logger2 = java.util.logging.Logger.getLogger("com.hazelcast.networking");
    private static final java.util.logging.Logger logger3 = java.util.logging.Logger.getLogger("com.hazelcast.server");
    static {
        java.util.logging.ConsoleHandler ch = new java.util.logging.ConsoleHandler();
        ch.setLevel(java.util.logging.Level.ALL);
        logger.addHandler(ch);
        logger.setLevel(java.util.logging.Level.ALL);
        logger2.addHandler(ch);
        logger2.setLevel(java.util.logging.Level.ALL);
        logger3.addHandler(ch);
        logger3.setLevel(java.util.logging.Level.ALL);
    }

}
