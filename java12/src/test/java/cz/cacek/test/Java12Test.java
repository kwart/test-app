package cz.cacek.test;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.teeing;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://www.azul.com/blog/39-new-features-and-apis-in-jdk-12/">azul blog</a>.
 */
public class Java12Test {

    @Test
    // https://openjdk.org/jeps/325
    @Disabled("Eclipse screams - both the case with arrows and switch expressions are available from Java 14")
    public void switchExpressionPreview() throws Exception {
        // Day day = MONDAY;
        // switch (day) {
        // case MONDAY, FRIDAY, SUNDAY -> System.out.println(6);
        // case TUESDAY -> System.out.println(7);
        // case THURSDAY, SATURDAY -> System.out.println(8);
        // case WEDNESDAY -> System.out.println(9);
        // }
        // var s = "Bar"
        // int result = switch (s) {
        // case "Foo":
        // break 1;
        // case "Bar":
        // break 2;
        // default:
        // System.out.println("Neither Foo nor Bar, hmmm...");
        // break 0;
        // };
    }

    @Test
    public void teeingCollector() throws Exception {
        long avg = Stream.of(1, 4, 2, 7, 4, 6, 5).collect(teeing(summingInt(i -> i), counting(), (sum, n) -> sum / n));
        assertEquals(4, avg);
    }

    @Test
    public void arrayType() throws Exception {
        Class<?> at = String.class.arrayType();
        assertEquals((new String[0]).getClass(), at);
    }

    enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    }
}
