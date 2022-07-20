package cz.cacek.test;

import java.util.concurrent.TimeUnit;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class App {

    public static void main(String[] args) throws Exception {
        boolean passed = false;
        try {
            HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(createConfig(5801, 5701));
            HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(createConfig(5901, 5701, 5801));
            HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(createConfig(5701));
            TimeUnit.SECONDS.sleep(5);
            assertClusterSize(2, hz2, hz3);
            assertClusterSize(1, hz1);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create("dev&&127.0.0.1:5901", MediaType.get("text/plain"));
            Request request = new Request.Builder().url("http://localhost:5701/hazelcast/rest/config/tcp-ip/member-list")
                    .post(body).build();
            try (Response response = client.newCall(request).execute()) {
                System.out.println("MemberList update response: " + response.body().string());
            }

            TimeUnit.SECONDS.sleep(60);
            assertClusterSize(3, hz1, hz2, hz3);
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
        networkConfig.getRestApiConfig().setEnabled(true).enableAllGroups();
        JoinConfig join = networkConfig.getJoin();
        join.getAutoDetectionConfig().setEnabled(false);
        join.getMulticastConfig().setEnabled(false);
        TcpIpConfig tcpIpConfig = join.getTcpIpConfig().setEnabled(true).clear().addMember("localhost:" + port);
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