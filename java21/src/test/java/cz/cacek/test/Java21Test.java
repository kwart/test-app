package cz.cacek.test;

import java.time.LocalTime;
import java.time.format.*;
import java.util.*;
import java.util.function.Function;
import java.lang.foreign.*;
import java.lang.invoke.*;

import org.junit.jupiter.api.Test;

import static java.lang.foreign.ValueLayout.*;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/21/">JDK 21 page</a> and
 * the <a href="https://www.happycoders.eu/java/java-21-features/">artictle at happycoders.eu</a>.
 */
public class Java21Test {

    // https://openjdk.org/projects/jdk/21/

    @Test
    // https://openjdk.org/jeps/430
    public void stringTempatesPreview() throws Exception {
        String time = STR."The time is \{
                // The java.time.format package is very useful
                DateTimeFormatter
                  .ofPattern("HH:mm:ss")
                  .format(LocalTime.now())
            } right now";
        System.out.println(time);

        String title = "My Web Page";
        String text  = "Hello, world";
        String html = STR."""
                <html>
                  <head>
                    <title>\{title}</title>
                  </head>
                  <body>
                    <p>\{text}</p>
                  </body>
                </html>
                """;
        System.out.println(html);
    }

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
    // https://openjdk.org/jeps/442
    // https://www.happycoders.eu/java/java-21-features/
    public void foreignFunctionAndMemoryApiPreview() {
        // 1. Get a lookup object for commonly used libraries
        SymbolLookup stdlib = Linker.nativeLinker().defaultLookup();

        // 2. Get a handle to the "strlen" function in the C standard library
        MethodHandle strlen = Linker.nativeLinker().downcallHandle(
            stdlib.find("strlen").orElseThrow(),
            FunctionDescriptor.of(JAVA_LONG, ADDRESS));

        // 3. Convert Java String to C string and store it in off-heap memory
        try (Arena offHeap = Arena.ofConfined()) {
          MemorySegment str = offHeap.allocateUtf8String("Happy Coding!");

          // 4. Invoke the foreign function
          long len = (long) strlen.invoke(str);

          System.out.println("len = " + len);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // 5. Off-heap memory is deallocated at end of try-with-resources
    }

    
    sealed abstract class Ball permits RedBall, BlueBall, GreenBall { }
    final  class RedBall   extends Ball { }
    final  class BlueBall  extends Ball { }
    final  class GreenBall extends Ball { }
    record Box<T extends Ball>(T content) { }

    @Test
    // https://openjdk.org/jeps/443
    public void unnamedPatternsAndVariablesPreview() {
        Box<? extends Ball> b = new Box(new GreenBall());
        switch (b) {
            case Box(RedBall _), Box(BlueBall _) -> System.out.println("Box: RB");
            case Box(GreenBall _)                -> System.out.println("Box: G");
            case Box(_)                          -> System.out.println("Box: ???");
        }
        
        for (int i = 0, _ = String.valueOf(new Object()); i < 3; i++) { 
            System.out.println(i);
        }
    }
}
