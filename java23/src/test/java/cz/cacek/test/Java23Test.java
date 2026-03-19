package cz.cacek.test;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/23/">JDK 23 page</a>.
 *
 * ## Markdown in Javadoc (JEP 467)
 *
 * This Javadoc comment itself uses **Markdown** syntax, which is a new feature in JDK 23.
 * - List item one
 * - List item two
 *
 * ```java
 * // Code blocks in Javadoc
 * System.out.println("Hello from JDK 23!");
 * ```
 */
public class Java23Test {

    // https://openjdk.org/projects/jdk/23/

    /// This method-level Javadoc also uses **Markdown** (JEP 467).
    /// The `///` comment style is new in JDK 23.
    @Test
    // https://openjdk.org/jeps/455
    public void primitiveTypesInPatternsPreview() {
        // Primitive type patterns in instanceof
        Object obj = 42;
        if (obj instanceof int i) {
            assertEquals(42, i);
        }

        // Primitive type patterns in switch
        String result = switch (obj) {
            case int i when i > 0 -> "positive int: " + i;
            case int i -> "non-positive int: " + i;
            default -> "not an int";
        };
        assertEquals("positive int: 42", result);

        // Primitive narrowing in switch
        long value = 100L;
        String narrowed = switch (value) {
            case int i -> "fits in int: " + i;
            default -> "too large for int";
        };
        assertEquals("fits in int: 100", narrowed);
    }

    @Test
    // https://openjdk.org/jeps/473
    public void streamGatherersSecondPreview() {
        // Stream Gatherers continue as second preview with same API
        List<List<String>> windows = Stream.of("a", "b", "c", "d", "e")
                .gather(Gatherers.windowFixed(2))
                .toList();
        assertEquals(List.of(List.of("a", "b"), List.of("c", "d"), List.of("e")), windows);

        // mapConcurrent - concurrent mapping with bounded concurrency
        List<String> mapped = Stream.of("hello", "world")
                .gather(Gatherers.mapConcurrent(2, String::toUpperCase))
                .toList();
        assertTrue(mapped.containsAll(List.of("HELLO", "WORLD")));
    }

    @Test
    // https://openjdk.org/jeps/482
    public void flexibleConstructorBodiesSecondPreview() {
        class Base {
            final int value;
            Base(int value) {
                this.value = value;
            }
        }
        class Derived extends Base {
            final String label;
            Derived(int value, String label) {
                // Validate and compute before super()
                Objects.requireNonNull(label, "label must not be null");
                var computedValue = value * 2;
                super(computedValue);
                this.label = label;
            }
        }
        Derived d = new Derived(5, "test");
        assertEquals(10, d.value);
        assertEquals("test", d.label);
    }
}
