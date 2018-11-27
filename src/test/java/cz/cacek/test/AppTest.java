package cz.cacek.test;

import static java.lang.String.format;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.instance.HazelcastInstanceFactory;

/**
 * A test template.
 */
public class AppTest {

    @Test
    public void testValid() {
        testInternal("valid.p12", 2);
    }

    @Test
    public void testExpired() {
        testInternal("expired.p12", 1);
    }

    @Test
    public void testNotYetValid() {
        testInternal("notYetValid.p12", 1);
    }
    
    @Test
    public void testValidWithCA() {
        testWithCA("localhost.p12", 2);
    }
    
    @Test
    public void testExpiredWithCA() {
        testWithCA("expired.p12", 1);
    }
    
    @Test
    public void testNotYetValidWithCA() {
        testWithCA("notYetValid.p12", 1);
    }

    private void testInternal(String keyStore, int expectedSize) {
        Config config = createConfig(keyStore);
        try {
            HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
            HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
            assertClusterSize(expectedSize, hz1, hz2);
        } finally {
            HazelcastInstanceFactory.terminateAll();
        }
    }
    
    private void testWithCA(String keyStore, int expectedSize) {
        String path = "src/test/resources/openssl/"+keyStore;
        Config config = new Config();
        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setEnabled(true)
            .setProperty("keyStore", path)
            .setProperty("keyStorePassword", "123456")
            .setProperty("keyStoreType", "PKCS12")
            .setProperty("trustStore", "src/test/resources/openssl/ca.p12")
            .setProperty("trustStorePassword", "123456")
            .setProperty("trustStoreType", "PKCS12")
        ;
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setSSLConfig(sslConfig).getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.setSSLConfig(sslConfig).getJoin().getTcpIpConfig().setEnabled(true).addMember("127.0.0.1:5701").setConnectionTimeoutSeconds(5);
        try {
            HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
            HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
            assertClusterSize(expectedSize, hz1, hz2);
        } finally {
            HazelcastInstanceFactory.terminateAll();
        }
    }

    private Config createConfig(String keyStore) {
        String path = "src/test/resources/"+keyStore;
        Config config = new Config();
        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setEnabled(true)
            .setProperty("keyStore", path)
            .setProperty("keyStorePassword", "123456")
            .setProperty("keyStoreType", "PKCS12")
            .setProperty("trustStore", path)
            .setProperty("trustStorePassword", "123456")
            .setProperty("trustStoreType", "PKCS12")
        ;
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setSSLConfig(sslConfig).getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.setSSLConfig(sslConfig).getJoin().getTcpIpConfig().setEnabled(true).addMember("127.0.0.1:5701").setConnectionTimeoutSeconds(5);
        return config ;
    }

    public static void assertClusterSize(int expectedSize, HazelcastInstance... instances) {
        for (int i = 0; i < instances.length; i++) {
            int clusterSize = getClusterSize(instances[i]);
            if (expectedSize != clusterSize) {
                fail(format("Cluster size is not correct. Expected: %d, actual: %d, instance index: %d",
                        expectedSize, clusterSize, i));
            }
        }
    }

    private static int getClusterSize(HazelcastInstance instance) {
        Set<Member> members = instance.getCluster().getMembers();
        return members == null ? 0 : members.size();
    }

}
