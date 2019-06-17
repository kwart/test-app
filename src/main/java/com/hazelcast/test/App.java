package com.hazelcast.test;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import com.hazelcast.core.Hazelcast;

public class App {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        @Setup(Level.Trial)
        public void setUp() {
            Hazelcast.newHazelcastInstance().shutdown();
        }
    }


    @Benchmark
    public static void test(ExecutionPlan plan, Blackhole bh) throws Exception {
        long[] numbers = new long[2<<20];
        for (int i=0; i<numbers.length; i++) {
            numbers[i] = i;
        }
        for (int i=0; i<numbers.length; i++) {
            byte[] bytes = Long.toString(numbers[i]).getBytes("UTF-8");
            bh.consume(MessageDigest.getInstance("SHA-1").digest(bytes));
        }
    }
}
