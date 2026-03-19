package cz.cacek.test;

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/24/">JDK 24 page</a>.
 */
public class Java24Test {

    // https://openjdk.org/projects/jdk/24/

    @Test
    // https://openjdk.org/jeps/485
    public void streamGatherersFinal() {
        // Stream Gatherers are now a final feature
        // Fixed-size windows
        List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5, 6)
                .gather(Gatherers.windowFixed(3))
                .toList();
        assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6)), windows);

        // Sliding windows
        List<List<Integer>> sliding = Stream.of(1, 2, 3, 4)
                .gather(Gatherers.windowSliding(2))
                .toList();
        assertEquals(List.of(List.of(1, 2), List.of(2, 3), List.of(3, 4)), sliding);

        // scan (prefix scan / running total)
        List<String> scanned = Stream.of("a", "b", "c")
                .gather(Gatherers.scan(() -> "", (acc, el) -> acc + el))
                .toList();
        assertEquals(List.of("a", "ab", "abc"), scanned);

        // fold (reduce to single value)
        Optional<Integer> sum = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.fold(() -> 0, Integer::sum))
                .findFirst();
        assertEquals(15, sum.orElse(0));

        // mapConcurrent - concurrent mapping
        List<String> mapped = Stream.of("hello", "world")
                .gather(Gatherers.mapConcurrent(4, String::toUpperCase))
                .toList();
        assertTrue(mapped.containsAll(List.of("HELLO", "WORLD")));
    }

    @Test
    // https://openjdk.org/jeps/484
    public void classFileApi() {
        // Class-File API is now final - can read, write, and transform class files
        // Build a simple class file programmatically
        byte[] classBytes = ClassFile.of().build(
                java.lang.constant.ClassDesc.of("cz.cacek.test.GeneratedClass"),
                classBuilder -> {
                    classBuilder.withFlags(ClassFile.ACC_PUBLIC);
                    // Add a static method that returns an int
                    classBuilder.withMethod("getValue",
                            java.lang.constant.MethodTypeDesc.of(
                                    java.lang.constant.ClassDesc.ofDescriptor("I")),
                            ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                            methodBuilder -> methodBuilder.withCode(codeBuilder -> {
                                codeBuilder.bipush(42);
                                codeBuilder.ireturn();
                            }));
                });

        // Parse the generated class file
        ClassModel classModel = ClassFile.of().parse(classBytes);
        assertEquals("cz/cacek/test/GeneratedClass", classModel.thisClass().asInternalName());

        // Verify the method exists
        boolean hasGetValue = classModel.methods().stream()
                .anyMatch(m -> m.methodName().equalsString("getValue"));
        assertTrue(hasGetValue);
    }

    @Test
    // https://openjdk.org/jeps/491
    public void synchronizeVirtualThreadsWithoutPinning() throws Exception {
        // Virtual threads no longer pin platform threads when synchronized
        Object lock = new Object();
        var results = Collections.synchronizedList(new ArrayList<String>());

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            Thread t = Thread.ofVirtual().start(() -> {
                synchronized (lock) {
                    results.add("vt-" + idx);
                }
            });
            threads.add(t);
        }
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(10, results.size());
    }
}
