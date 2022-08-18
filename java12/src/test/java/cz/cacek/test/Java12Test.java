package cz.cacek.test;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.teeing;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://www.azul.com/blog/39-new-features-and-apis-in-jdk-12/">azul blog</a>.
 */
public class Java12Test {

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
}
