package cz.cacek.test;

import java.util.Map;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.ServerSocketEndpointConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.discovery.uds.UDSDiscoveryStrategyFactory;
import com.hazelcast.spi.properties.ClusterProperty;

public class App {

    public static void main(String[] args) {
        System.setProperty("hazelcast.tracking.server", "true");
        System.setProperty("hazelcast.unix.socket.dir", "/home/kwart/socketdir");
        HazelcastInstance hz = createMember(5555);
        hz.getMap("test").put("a", "b");
        System.out.println(createMember(5556).getMap("test").get("a"));
    }

    protected static HazelcastInstance createMember(int clientPort) {
        Config config = new Config();
        config.getMetricsConfig().setEnabled(false);
        config.getAdvancedNetworkConfig()
              .setEnabled(true);
        // issue with advanced networking + no ClIENT endpoint
        config.getAdvancedNetworkConfig().setClientEndpointConfig(
                new ServerSocketEndpointConfig().setPort(5555)
        );
        config.getAdvancedNetworkConfig().getJoin().getDiscoveryConfig()
              .addDiscoveryStrategyConfig(
                      new DiscoveryStrategyConfig("com.hazelcast.spi.discovery.uds.UDSDiscoveryStrategy",
                              Map.of(UDSDiscoveryStrategyFactory.UDS_SOCKET_DIRECTORY, "/home/kwart/socketdir")));
        config.setProperty(ClusterProperty.DISCOVERY_SPI_ENABLED.getName(), "true");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        return hz;
    }
}
