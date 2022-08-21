package cz.cacek.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.InstantSource;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import cz.cacek.untrusted.Gadget;

/**
 * Check the <a href="https://www.baeldung.com/java-17-new-features">baeldung blog</a>.
 */
public class Java17Test {

    // https://openjdk.org/jeps/306 Restore Always-Strict Floating-Point Semantics
    // JEP 382: New macOS Rendering Pipeline, JEP 391: macOS/AArch64 Port
    // https://openjdk.org/jeps/398 Deprecate the Applet API for Removal
    // https://openjdk.org/jeps/403 Strongly Encapsulate JDK Internals
    // https://openjdk.org/jeps/403 Pattern Matching for switch (Preview)
    // https://openjdk.org/jeps/411 Deprecate the Security Manager for Removal

    @Test
    // https://openjdk.org/jeps/356
    // https://www.baeldung.com/java-17-random-number-generators
    public void enhancedPseudoRandomNumberGenerators() throws Exception {
        // returns an IntStream with size @streamSize of random numbers generated using the @algorithm
        // where the lower bound is 0 and the upper is 100 (exclusive)
        IntStream ints = RandomGeneratorFactory.getDefault().create().ints(50, 0, 100);
        long count = ints.peek(i -> assertThat(i, allOf(greaterThanOrEqualTo(0), lessThan(100)))).count();
        assertEquals(50, count);

        IntStream intsFromCustomAlgWithHardcodedSeed = RandomGeneratorFactory.of("L64X256MixRandom").create(0L).ints(3);
        assertEquals(Arrays.asList(1271863254, -314506048, -644178722), intsFromCustomAlgWithHardcodedSeed.boxed().toList());
    }

    @Test
    // https://openjdk.org/jeps/415
    public void contextSpecificDeserializationFilters() throws Exception {
        // Create a FilterInThread filter factory and set
        byte[] serbytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(new Trusted());
            oos.writeObject(new Gadget());
            oos.flush();
            serbytes = baos.toByteArray();
        }
        var filterInThread = new FilterInThread();
        ObjectInputFilter.Config.setSerialFilterFactory(filterInThread);

        // Create a filter to allow example.* classes and reject all others
        var filter = ObjectInputFilter.Config.createFilter("cz.cacek.test.*;java.base/*;!*");
        filterInThread.doWithSerialFilter(filter, () -> {
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serbytes));
                Object obTrusted = ois.readObject();
                assertInstanceOf(Trusted.class, obTrusted);
                InvalidClassException exc = assertThrows(InvalidClassException.class, () -> ois.readObject());
                assertThat(exc.getMessage(), containsString("REJECTED"));
            } catch (IOException | ClassNotFoundException e) {
                fail(e);
            }
        });
    }

    @Test
    // https://openjdk.org/jeps/409
    // https://www.baeldung.com/java-sealed-classes-interfaces
    public void sealedClasses() throws Exception {
        assertFalse(getClass().isSealed());
        assertTrue(Shape.class.isSealed());
        assertFalse(Circle.class.isSealed());
        assertTrue(Square.class.isSealed());
        assertFalse(BlueSquare.class.isSealed());
        assertThat(Shape.class.getPermittedSubclasses(),
                arrayContainingInAnyOrder(Circle.class, Rectangle.class, Square.class));
    }

    @Test
    public void javaTimeInstantSource() throws Exception {
        InstantSource instantSourceSystem = InstantSource.system();
        Instant instant1 = instantSourceSystem.instant();
        InstantSource instantSourceFixed = InstantSource.fixed(instant1);
        TimeUnit.MILLISECONDS.sleep(10);
        assertNotEquals(instant1, instantSourceSystem.instant());
        assertEquals(instant1, instantSourceFixed.instant());
    }

    @Test
    // https://www.baeldung.com/java-hexformat
    public void hexFormat() throws Exception {
        HexFormat hexFormat = HexFormat.of();
        assertEquals("61686f6a", hexFormat.formatHex("ahoj".getBytes(UTF_8)));
        assertEquals("000000ff", hexFormat.toHexDigits(255));
        assertEquals("[61]:[68]:[6F]:[6A]",
                HexFormat.ofDelimiter(":").withPrefix("[").withSuffix("]").withUpperCase().formatHex("ahoj".getBytes(UTF_8)));
    }

    // https://openjdk.org/jeps/409
    public static abstract sealed class Shape permits Circle, Rectangle, Square {
    }

    static non-sealed class Circle extends Shape {
    }

    static final class Rectangle extends Shape {
    }

    static sealed class Square extends Shape permits BlueSquare {
    }

    static final class BlueSquare extends Square {
    }

    /**
     * Example from JEP 415
     */
    public static class FilterInThread implements BinaryOperator<ObjectInputFilter> {

        // ThreadLocal to hold the serial filter to be applied
        private final ThreadLocal<ObjectInputFilter> filterThreadLocal = new ThreadLocal<>();

        // Construct a FilterInThread deserialization filter factory.
        public FilterInThread() {
        }

        /**
         * The filter factory, which is invoked every time a new ObjectInputStream is created. If a per-stream filter is already
         * set then it returns a filter that combines the results of invoking each filter.
         *
         * @param curr the current filter on the stream
         * @param next a per stream filter
         * @return the selected filter
         */
        @Override
        public ObjectInputFilter apply(ObjectInputFilter curr, ObjectInputFilter next) {
            if (curr == null) {
                // Called from the OIS constructor or perhaps OIS.setObjectInputFilter with no current filter
                var filter = filterThreadLocal.get();
                if (filter != null) {
                    // Prepend a filter to assert that all classes have been Allowed or Rejected
                    filter = ObjectInputFilter.rejectUndecidedClass(filter);
                }
                if (next != null) {
                    // Prepend the next filter to the thread filter, if any
                    // Initially this is the static JVM-wide filter passed from the OIS constructor
                    // Append the filter to reject all UNDECIDED results
                    filter = ObjectInputFilter.merge(next, filter);
                    filter = ObjectInputFilter.rejectUndecidedClass(filter);
                }
                return filter;
            } else {
                // Called from OIS.setObjectInputFilter with a current filter and a stream-specific filter.
                // The curr filter already incorporates the thread filter and static JVM-wide filter
                // and rejection of undecided classes
                // If there is a stream-specific filter prepend it and a filter to recheck for undecided
                if (next != null) {
                    next = ObjectInputFilter.merge(next, curr);
                    next = ObjectInputFilter.rejectUndecidedClass(next);
                    return next;
                }
                return curr;
            }
        }

        /**
         * Apply the filter and invoke the runnable.
         *
         * @param filter the serial filter to apply to every deserialization in the thread
         * @param runnable a Runnable to invoke
         */
        public void doWithSerialFilter(ObjectInputFilter filter, Runnable runnable) {
            var prevFilter = filterThreadLocal.get();
            try {
                filterThreadLocal.set(filter);
                runnable.run();
            } finally {
                filterThreadLocal.set(prevFilter);
            }
        }
    }
}
