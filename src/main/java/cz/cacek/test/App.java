package cz.cacek.test;

import java.util.concurrent.TimeUnit;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;

public class App {

    public static void main(String[] args) throws Exception {
        boolean passed = false;
        try {
            HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(createConfig(5701, 5801));
            HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(createConfig(5801, 5801));
            TimeUnit.SECONDS.sleep(60);
            assertClusterSize(3, hz1, hz2);
            passed = true;
        } finally {
            HazelcastInstanceFactory.terminateAll();
            System.out.println(passed ? ">>> OK" : ">>> FAILED");
        }
    }

    protected static Config createConfig(int port, int... otherPorts) {
        Config config = new Config().setProperty("hazelcast.member.naming.moby.enabled", "false")
                .setProperty("hazelcast.merge.first.run.delay.seconds", "30")
                .setProperty("hazelcast.merge.next.run.delay.seconds", "20");
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPortAutoIncrement(false).setPort(port);
        JoinConfig join = networkConfig.getJoin();
        join.getAutoDetectionConfig().setEnabled(false);
        join.getMulticastConfig().setEnabled(false);
        TcpIpConfig tcpIpConfig = join.getTcpIpConfig().setEnabled(true).clear();
        for (int otherPort : otherPorts) {
            tcpIpConfig.addMember("localhost:" + otherPort);
        }
        return config;
    }

    public static void assertClusterSize(int expectedSize, HazelcastInstance... instances) {
        for (int i = 0; i < instances.length; i++) {
            int clusterSize = instances[i].getCluster().getMembers().size();
            if (expectedSize != clusterSize) {
                throw new RuntimeException(
                        String.format("Cluster size is not correct. Expected: %d, actual: %d, instance index: %d", expectedSize,
                                clusterSize, i));
            }
        }
    }
}