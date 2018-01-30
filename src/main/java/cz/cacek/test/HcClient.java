package cz.cacek.test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;

public class HcClient {
    public static void main(String[] args) {
        String hazelcastGroup = "test";
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setProperty("hazelcast.discovery.enabled", "true");
        clientConfig.getGroupConfig().setName(hazelcastGroup).setPassword(hazelcastGroup);

        DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(
                "com.hazelcast.spi.discovery.multicast.MulticastDiscoveryStrategy");
        clientConfig.getNetworkConfig().getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
        HazelcastClient.newHazelcastClient(clientConfig);
    }

}