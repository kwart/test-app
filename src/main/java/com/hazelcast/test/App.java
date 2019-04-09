package com.hazelcast.test;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;

import com.hazelcast.bitset.BitSetService;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IBitSet;
import com.hazelcast.core.ISet;

/**
 * @author Josef Cacek
 */
public class App {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

//        @Param({ "10", "1000", "1000000" })
        public int aclLength;

        public HazelcastInstance hz;

        @Setup(Level.Trial)
        public void setUp() {
            Config config = new Config();
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            hz = Hazelcast.newHazelcastInstance(config);
            for (int i=1; i<5; i++) {
                String name = "acl"+i;
                ISet<Integer> iset = hz.getSet(name);
                IBitSet bitSet = hz.getBitSet(name);
                for (int j=0; j<1024; j++) {
                    if ((j % (i+1))==0) {
                        iset.add(j);
                        bitSet.set(j);
                    }
                }
            }
        }

        @Setup(Level.Iteration)
        public void setUpIt() {
            String name = "acl0";
            ISet<Integer> iset = hz.getSet(name);
            IBitSet bitSet = hz.getBitSet(name);
            for (int j=0; j<1024; j++) {
                iset.add(j);
                bitSet.set(j);
            }
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            hz.getLifecycleService().terminate();
        }
    }


    @Benchmark
    @Threads(3)
    public static void setBasedAcl(ExecutionPlan plan) {
        ISet set = plan.hz.getSet("acl0");
        for (int i=1; i<5; i++) {
            set.retainAll(plan.hz.getSet("acl"+i));
        }
    }

    @Benchmark
    @Threads(3)
    public static void bitSetBasedAcl(ExecutionPlan plan) {
        IBitSet bitSet = plan.hz.getBitSet("acl0");
        for (int i=1; i<5; i++) {
            bitSet.and("acl"+i);
        }
    }
}
