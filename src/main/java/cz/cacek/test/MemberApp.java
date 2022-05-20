package cz.cacek.test;

import com.hazelcast.core.Hazelcast;

/**
 * The App!
 */
public class MemberApp {

    public static void main(String[] args) throws Exception {
        Hazelcast.newHazelcastInstance();
    }
}
