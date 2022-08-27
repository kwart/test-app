package cz.cacek.test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/19/">JDK 19 page</a>.
 */
public class Java19Test {

    // https://openjdk.org/projects/jdk/19/

    @Test
    // https://openjdk.org/jeps/405
    public void recordPatternsPreview() throws Exception {
        ColoredPoint cp = new ColoredPoint(new Point(1,2), Color.RED);
        Object r = new Rectangle(cp, cp);
        if (r instanceof Rectangle(ColoredPoint(Point p, Color c), ColoredPoint lr)) {
             System.out.println(c);
        }
    }

    @Test
    // https://openjdk.org/jeps/425
    public void virtualThreadsPreview() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10_000).forEach(i -> {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofSeconds(1));
                    return i;
                });
            });
        }  // executor.close() is called implicitly, and waits
    }

    record Point(int x, int y) {
    }

    enum Color {
        RED, GREEN, BLUE
    }

    record ColoredPoint(Point p, Color c) {
    }

    record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {
    }
}
