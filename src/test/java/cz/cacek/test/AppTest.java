package cz.cacek.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.model.Network.Ipam;

/**
 * A test template.
 */
public class AppTest {
    @Rule
    public Network network = Network.builder().createNetworkCmdModifier(
            cmd -> cmd.withName("toxiproxytest").withIpam(new Ipam().withConfig(new Ipam.Config().withSubnet("172.18.5.0/24"))))
            .build();
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));

    @Rule
    public ToxiproxyContainer toxiproxy = new ToxiproxyContainer(DockerImageName.parse("ghcr.io/shopify/toxiproxy:2.4.0"))
            .withEnv("GODEBUG", "schedtrace=2000")
            .withNetwork(network).withNetworkAliases("member-proxy");

    protected final List<ToxiproxyContainer.ContainerProxy> memberProxies = new ArrayList<>();

    @Test
    public void test() throws Exception {
        System.out.println(getIpAddress(toxiproxy));
        memberProxies.add(toxiproxy.getProxy("member0", 5701));
        memberProxies.add(toxiproxy.getProxy("member1", 5701));
        memberProxies.add(toxiproxy.getProxy("member2", 5701));

        System.out.println("starting container");
        GenericContainer[] hz = new GenericContainer[] { startHz(0), startHz(1), startHz(2) };

        // wait for Hazelcast cluster to establish/join
        TimeUnit.SECONDS.sleep(30);
        System.out.println("cutting connections");
        for (ToxiproxyContainer.ContainerProxy memberProxy: memberProxies) {
            memberProxy.setConnectionCut(true);
        }
        System.out.println("connections were cut");

        // wait for Hazelcast split-brain detection
        TimeUnit.SECONDS.sleep(60);
        for (ToxiproxyContainer.ContainerProxy memberProxy: memberProxies) {
            System.out.println("removing a toxic");
            memberProxy.setConnectionCut(false);
        }

        System.out.println("all toxics were removed");
        TimeUnit.SECONDS.sleep(60);
    }

    private GenericContainer startHz(int idx) {
        GenericContainer hzc = new GenericContainer("hazelcast/hazelcast:5.1.2-slim")
                .withNetwork(network)
                .withNetworkAliases("member" + idx)
                .withEnv("HZ_PHONE_HOME_ENABLED", "false")
                .withEnv("JAVA_OPTS", getJavaOpts(idx))
                .withFileSystemBind("hazelcast-config.xml", "/opt/hazelcast/config/hazelcast-docker.xml", BindMode.READ_ONLY);
        hzc.start();
        return hzc;
    }

    private String getJavaOpts(int idx) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int proxyPort = memberProxies.get(i).getOriginalProxyPort();
            sb.append(" -DproxyPort").append(i).append("=").append(proxyPort);
            if (idx == i) {
                sb.append(" -DproxyPort").append("=").append(proxyPort);
            }
        }
        return sb.toString();
    }

    private static String getIpAddress(ToxiproxyContainer cont) {
        return cont.getContainerInfo().getNetworkSettings().getNetworks().values().iterator().next().getIpAddress();
    }

}
