package cz.cacek.test;

import java.util.*;

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
    // https://openjdk.org/jeps/454
    public void foreignFunctionAndMemoryApi() throws Throwable {
        // FFM API is final in JDK 22
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
