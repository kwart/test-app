package cz.cacek.test;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

/**
 * The App!
 */
public class LiteMemberApp {

    public static void main(String[] args) throws Exception {
        Config config = new Config().setLiteMember(true);
        Hazelcast.newHazelcastInstance(config);
    }
}
