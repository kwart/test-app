package cz.cacek.test;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/22/">JDK 22 page</a>.
 */
public class Java22Test {

    // https://openjdk.org/projects/jdk/22/

    @Test
    // https://openjdk.org/jeps/456
    public void unnamedVariablesAndPatterns() {
        record Point(int x, int y) {}
        Object obj = new Point(1, 2);

        // Unnamed pattern variable in instanceof
        if (obj instanceof Point(var x, _)) {
            assertEquals(1, x);
        }

        // Unnamed variable in switch
        String result = switch (obj) {
            case Point(var x, _) when x > 0 -> "positive x";
            case Point _ -> "other point";
            default -> "not a point";
        };
        assertEquals("positive x", result);

        // Unnamed variable in enhanced for
        int count = 0;
        for (var _ : List.of("a", "b", "c")) {
            count++;
        }
        assertEquals(3, count);

        // Unnamed variable in try-with-resources
        try (var _ = new AutoCloseable() {
            public void close() { }
        }) {
            assertTrue(true);
        }

        // Unnamed variable in catch
        try {
            Integer.parseInt("not a number");
            fail("Should have thrown");
        } catch (NumberFormatException _) {
            assertTrue(true);
        }
    }

    @Test
    // https://openjdk.org/jeps/461
    public void streamGatherersPreview() {
        // Fixed-size windows
        List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowFixed(2))
                .toList();
        assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), windows);

        // Sliding windows
        List<List<Integer>> sliding = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowSliding(3))
                .toList();
        assertEquals(List.of(List.of(1, 2, 3), List.of(2, 3, 4), List.of(3, 4, 5)), sliding);

        // fold
        Optional<String> folded = Stream.of("a", "b", "c")
                .gather(Gatherers.fold(() -> "", (acc, el) -> acc + el))
                .findFirst();
        assertEquals("abc", folded.orElse(""));

        // scan (running accumulation)
        List<Integer> scanned = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.scan(() -> 0, Integer::sum))
                .toList();
        assertEquals(List.of(1, 3, 6, 10, 15), scanned);
    }

    @Test
    // https://openjdk.org/jeps/447
    public void statementsBeforeSuperPreview() {
        // Statements before super(...) allow validation before calling the super constructor
        class Shape {
            final String name;
            Shape(String name) {
                this.name = name;
            }
        }
        class Circle extends Shape {
            final double radius;
            Circle(String name, double radius) {
                // Statements before super() - new in JDK 22 preview
                if (radius <= 0) throw new IllegalArgumentException("radius must be positive");
                super(name);
                this.radius = radius;
            }
        }
        Circle c = new Circle("circle", 5.0);
        assertEquals("circle", c.name);
        assertEquals(5.0, c.radius);
        assertThrows(IllegalArgumentException.class, () -> new Circle("bad", -1));
    }

    @Test
    // https://openjdk.org/jeps/454
    public void foreignFunctionAndMemoryApiFinal() throws Throwable {
        // FFM API is now final (no longer preview)
        java.lang.foreign.SymbolLookup stdlib = java.lang.foreign.Linker.nativeLinker().defaultLookup();
        java.lang.invoke.MethodHandle strlen = java.lang.foreign.Linker.nativeLinker().downcallHandle(
                stdlib.find("strlen").orElseThrow(),
                java.lang.foreign.FunctionDescriptor.of(
                        java.lang.foreign.ValueLayout.JAVA_LONG,
                        java.lang.foreign.ValueLayout.ADDRESS));

        try (java.lang.foreign.Arena offHeap = java.lang.foreign.Arena.ofConfined()) {
            java.lang.foreign.MemorySegment str = offHeap.allocateFrom("Hello JDK 22!");
            long len = (long) strlen.invoke(str);
            assertEquals(13, len);
        }
    }
}
