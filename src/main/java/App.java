

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * The App!
 */
public class App {

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            usage("Two parameters expected!");
        }

        String clusterName = args[0];
        String member = args[1];

        Config config = Config.load().setClusterName(clusterName);
        config.setProperty("hazelcast.discovery.enabled", "false").setLiteMember(true);
        config.getSecurityConfig().setEnabled(true);
        JoinConfig join = config.getAdvancedNetworkConfig().setEnabled(true).getJoin();
        join.getTcpIpConfig().setEnabled(true).addMember(member);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        hz.getCluster().shutdown();
    }

    private static void usage(String msg) {
        System.err.println(msg);
        System.err.println();
        System.err.println("Usage:");
        System.err.println("\tjava -jar <appname.jar> <clusterName> <memberAddress>");
        System.err.println();
        System.exit(1);
    }

}
