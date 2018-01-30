package cz.cacek.test;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;

public class HCServer {

    public static void main(String[] args) {
        String hazelcastGroup = "test";
        Config config = new Config();
        config.setProperty("hazelcast.discovery.enabled", "true");
        config.getGroupConfig().setName(hazelcastGroup).setPassword(hazelcastGroup);

        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig("com.hazelcast.spi.discovery.multicast.MulticastDiscoveryStrategy");
        joinConfig.getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
        joinConfig.getMulticastConfig().setEnabled(false);
        Hazelcast.newHazelcastInstance(config);
    }
}