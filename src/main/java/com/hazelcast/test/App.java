package com.hazelcast.test;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

/**
 * @author Josef Cacek
 */
public class App {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

//        @Param({ "10", "1000", "1000000" })
        @Param({ "100" })
        public int classesInPackage;

        @Param({ "true", "false" })
        public boolean origImplementation;

        public Listed classFilter;

        @Setup(Level.Trial)
        public void setUp() {
            if (origImplementation) {
                classFilter = new ClassFilter().addClasses("com.testclass.Test").addPackages("com.testpackage");
            } else {
                classFilter = new ClassFilter2().addClasses("com.testclass.Test").addPackages("com.testpackage");
            }
        }
    }

//    @Benchmark
//    @Threads(3)
//    public static void classListed(ExecutionPlan plan) {
//        plan.classFilter.isListed("com.testclass.Test");
//    }
//
//    @Benchmark
//    @Threads(3)
//    public static void classNotListed(ExecutionPlan plan) {
//        plan.classFilter.isListed("com.testclass.XTest");
//    }

    @Benchmark
    @Threads(3)
    public static void packageListed(ExecutionPlan plan) {
        for (int i=0; i<plan.classesInPackage; i++) {
            plan.classFilter.isListed("com.testpackage."+ i);
        }
    }
//
//    @Benchmark
//    @Threads(3)
//    public static void packageNotListed(ExecutionPlan plan) {
//        for (int i=0; i<plan.classesInPackage; i++) {
//            plan.classFilter.isListed("com.anotherpackage."+ i);
//        }
//    }
}
