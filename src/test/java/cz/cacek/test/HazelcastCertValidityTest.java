package cz.cacek.test;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.instance.HazelcastInstanceFactory;
import com.hazelcast.nio.ssl.OpenSSLEngineFactory;

import io.netty.handler.ssl.OpenSsl;

/**
 * A test for TLS certificates validity handling in Hazelcast (i.e. in Java).
 */
@RunWith(Parameterized.class)
public class HazelcastCertValidityTest {

    enum OpenSslMode {
        NONE, TRUSTMANAGER, NATIVE
    }

    @Parameter
    public boolean mutualAuthentication;

    @Parameter(value = 1)
    public OpenSslMode openSslMode;

    @Parameters(name = "mutualAuthentication:{0} openSslMode:{1}")
    public static Collection<Object[]> parameters() {
        return asList(new Object[][]{
                {false, OpenSslMode.NONE},
                {false, OpenSslMode.TRUSTMANAGER},
                {false, OpenSslMode.NATIVE},
                {true, OpenSslMode.NONE},
                {true, OpenSslMode.TRUSTMANAGER},
                {true, OpenSslMode.NATIVE},
        });
    }

    @Test
    public void testValidSelfSigned() {
        testInternal(true, "valid", 2);
    }

    @Test
    public void testExpiredSelfSigned() {
        testInternal(true, "expired", 2);
    }

    @Test
    public void testNotYetValidSelfSigned() {
        testInternal(true, "notYetValid", 2);
    }

    @Test
    public void testValidWithCA() {
        testInternal(false, "localhost", 2);
    }

    @Test
    public void testExpiredWithCA() {
        testInternal(false, "expired", 1);
    }

    @Test
    public void testNotYetValidWithCA() {
        testInternal(false, "notYetValid", 1);
    }

    private void testInternal(boolean selfSigned, String keyStore, int expectedSize) {
        assumeOpenSslModeSupported();
        Config config = new Config();
        SSLConfig sslConfig = new SSLConfig();
        String pathBase = "src/test/resources/";
        String trustStore = keyStore;
        if (! selfSigned) {
            pathBase = "src/test/resources/CAsigned/";
            trustStore = "ca";
        }
        if (openSslMode != OpenSslMode.NATIVE) {
            sslConfig.setEnabled(true)
                    .setProperty("keyStore", pathBase + keyStore + ".p12")
                    .setProperty("keyStorePassword", "123456")
                    .setProperty("keyStoreType", "PKCS12")
                    .setProperty("trustStore", pathBase + trustStore + ".p12")
                    .setProperty("trustStorePassword", "123456")
                    .setProperty("trustStoreType", "PKCS12");
        } else {
            sslConfig.setEnabled(true)
                    .setProperty(OpenSSLEngineFactory.KEY_FILE, pathBase + keyStore + "-key.pem")
                    .setProperty(OpenSSLEngineFactory.KEY_CERT_CHAIN_FILE, pathBase + keyStore + "-cert.pem")
                    .setProperty(OpenSSLEngineFactory.TRUST_CERT_COLLECTION_FILE, pathBase + trustStore + "-cert.pem")
                    ;
        }
        if (openSslMode != OpenSslMode.NONE) {
            sslConfig.setFactoryClassName(OpenSSLEngineFactory.class.getName());
        }
        if (mutualAuthentication) {
            sslConfig.setProperty("mutualAuthentication", "REQUIRED");
        }
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setSSLConfig(sslConfig).getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.setSSLConfig(sslConfig).getJoin().getTcpIpConfig().setEnabled(true).addMember("127.0.0.1:5701")
                .setConnectionTimeoutSeconds(5);
        try {
            HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
            HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
            assertClusterSize(expectedSize, hz1, hz2);
        } finally {
            HazelcastInstanceFactory.terminateAll();
        }
    }

    public static void assertClusterSize(int expectedSize, HazelcastInstance... instances) {
        for (int i = 0; i < instances.length; i++) {
            int clusterSize = getClusterSize(instances[i]);
            if (expectedSize != clusterSize) {
                fail(format("Cluster size is not correct. Expected: %d, actual: %d, instance index: %d", expectedSize,
                        clusterSize, i));
            }
        }
    }

    private static int getClusterSize(HazelcastInstance instance) {
        Set<Member> members = instance.getCluster().getMembers();
        return members == null ? 0 : members.size();
    }

    private void assumeOpenSslModeSupported() {
        String vendor = System.getProperty("java.vendor");
        assumeFalse("Test skipped for IBM Java", vendor.startsWith("IBM") && openSslMode==OpenSslMode.TRUSTMANAGER);
    }
}
