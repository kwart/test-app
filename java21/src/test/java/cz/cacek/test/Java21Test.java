package cz.cacek.test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/21/">JDK 21 page</a> and
 * the <a href="https://www.happycoders.eu/java/java-21-features/">article at happycoders.eu</a>.
 */
public class Java21Test {

    // https://openjdk.org/projects/jdk/21/

    @Test
    // https://openjdk.org/jeps/431
    public void sequencedCollections() throws Exception {
        sequencedCollectionTest(new ArrayList<Integer>());
        sequencedCollectionTest(new LinkedHashSet<Integer>());
    }

    private void sequencedCollectionTest(SequencedCollection<Integer> sc) {
        sc.addFirst(1);
        sc.addFirst(0);
        sc.addLast(2);

        System.out.println(sc.getFirst());
        System.out.println(sc.getLast());
        System.out.println(sc);
        System.out.println(sc.reversed());
    }


    record Point(int x, int y) {}
    enum Color { RED, GREEN, BLUE }
    record ColoredPoint(Point p, Color c) {}
    record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}

    @Test
    // https://openjdk.org/jeps/440
    public void recordPatterns() {
        Rectangle r = new Rectangle(new ColoredPoint(new Point(10, 10), Color.RED),
                new ColoredPoint(new Point(5, 7), Color.BLUE));
        if (r instanceof Rectangle(ColoredPoint(Point p, Color c),
                    ColoredPoint lr)) {
            System.out.println(c);
        }
        Object o = r;
        switch (o) {
            case ColoredPoint(var p, var c) -> {
                System.out.println(p);
            }
            case Rectangle(var u, var l) -> {
                System.out.println(u.c());
            }
            default -> {
                System.out.println("And now for something completely different");
            }
        }
    }

    @Test
    // https://openjdk.org/jeps/441
    public void patternMatchingForSwitch() {
        Function<Object, String> func = o ->
        switch (o) {
            case Integer i -> String.format("int %d", i);
            case Long l    -> String.format("long %d", l);
            case Double d  -> String.format("double %f", d);
            case String s when s.equals("Test") -> "Testing";
            case String s  -> String.format("String %s", s);
            case null      -> "42";
            default        -> o.toString();
        };
        System.out.println(func.apply("Test"));
        System.out.println(func.apply("Whatever"));
        System.out.println(func.apply(Long.MIN_VALUE));
        System.out.println(func.apply(new java.util.HashMap()));
        System.out.println(func.apply(null));
    }

    @Test
    // https://openjdk.org/jeps/444
    public void virtualThreads() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10_000).forEach(i -> {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofSeconds(1));
                    return i;
                });
            });
        }  // executor.close() is called implicitly, and waits
    }

    @Test
    // https://openjdk.org/jeps/452
    public void keyEncapsulationMechanismApi() throws Exception {
        // KEM API for key encapsulation
        var keyPairGen = java.security.KeyPairGenerator.getInstance("X25519");
        var keyPair = keyPairGen.generateKeyPair();
        System.out.println("KEM key pair algorithm: " + keyPair.getPublic().getAlgorithm());
    }
}
