package cz.cacek.test;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Starts a Hazelcast Member.
 */
public final class HazelcastServiceStarter {

    private static HazelcastInstance hz;

    private HazelcastServiceStarter() {
    }

    public static void start(String[] args) {
        hz = Hazelcast.newHazelcastInstance();
    }

    public static void stop(String[] args) {
        if (hz!=null) {
            hz.shutdown();
            hz = null;
        }
    }

}
