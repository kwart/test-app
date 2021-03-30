package com.hazelcast.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ServerSocketEndpointConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.spi.properties.ClusterProperty;

/**
 * Unix sockets test
 */
public class App {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        public HazelcastInstance hz;
        public Path tempDir;
        IMap<String, String> map;

        @Setup(Level.Trial)
        public void setUp() throws IOException {
            boolean isUnixSocketSupported = true;
            try {
                Class.forName("com.hazelcast.spi.discovery.uds.UDSDiscoveryStrategy");
            } catch (Exception e) {
                isUnixSocketSupported = false;
            }
            tempDir = Files.createTempDirectory("jmh-sockets-");
            System.setProperty("hazelcast.unix.socket.dir", tempDir.toAbsolutePath().toString());
            Hazelcast.newHazelcastInstance(createConfig(isUnixSocketSupported));
            Hazelcast.newHazelcastInstance(createConfig(isUnixSocketSupported));
            Hazelcast.newHazelcastInstance(createConfig(isUnixSocketSupported));

            Config liteConfig = createConfig(isUnixSocketSupported).setLiteMember(true);
            hz = Hazelcast.newHazelcastInstance(liteConfig);
        }

        protected Config createConfig(boolean isUnixSocketSupported) {
            Config config = new Config();
            config.getMetricsConfig().setEnabled(false);
            config.getAdvancedNetworkConfig().setEnabled(true);
            // issue with advanced networking + no ClIENT endpoint
            config.getAdvancedNetworkConfig().setClientEndpointConfig(new ServerSocketEndpointConfig().setPort(5555));
            JoinConfig joinConfig = config.getAdvancedNetworkConfig().getJoin();
            if (isUnixSocketSupported) {
                joinConfig.getDiscoveryConfig().addDiscoveryStrategyConfig(
                        new DiscoveryStrategyConfig("com.hazelcast.spi.discovery.uds.UDSDiscoveryStrategy",
                                Map.of("hazelcast.disco.udsDirectory", tempDir.toString())));
                config.setProperty(ClusterProperty.DISCOVERY_SPI_ENABLED.getName(), "true");
            } else {
                joinConfig.getAutoDetectionConfig().setEnabled(false);
                joinConfig.getMulticastConfig().setEnabled(false);
                joinConfig.getTcpIpConfig().setEnabled(true).addMember("127.0.0.1:5701");
            }
            return config;
        }

        @Setup(Level.Iteration)
        public void setUpIt() {
            hz.getMap("test").destroy();
            map = hz.getMap("test");
            map.put("key", "value");
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            hz.getCluster().shutdown();
            try {
                Runtime.getRuntime().exec("rm -rf '" + tempDir.toAbsolutePath() + "'");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Benchmark
    public String get(ExecutionPlan ep) {
        return ep.map.get("key");
    }

    @Benchmark
    public String put(ExecutionPlan ep) {
        return ep.map.put("key", "value-" + System.nanoTime());
    }
}
