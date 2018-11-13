package cz.cacek.test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;

/**
 * Hazelcast Hello world!
 */
public class App {

    public static void main(String[] args) {
        try {
            Config config = new Config();
//            config.setProperty("hazelcast.discovery.enabled", "true");
//            JoinConfig joinConfig = config.getNetworkConfig().getJoin();
//            joinConfig.getMulticastConfig().setEnabled(false);
//            DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig("com.hazelcast.eureka.one.EurekaOneDiscoveryStrategy");
//            discoveryStrategyConfig.addProperty("use-classpath-eureka-client-props", "false");
//            discoveryStrategyConfig.addProperty("shouldUseDns", "false");
//            discoveryStrategyConfig.addProperty("serviceUrl.default", "http://localhost:8080/eureka/v2/");
//            joinConfig.getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
            HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
            ClientConfig clientConfig = new ClientConfig();
//            ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();
//            networkConfig.getAddresses().clear();
//            networkConfig.getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
            HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        } finally {
            HazelcastClient.shutdownAll();
            HazelcastInstanceFactory.terminateAll();
        }
    }
}
