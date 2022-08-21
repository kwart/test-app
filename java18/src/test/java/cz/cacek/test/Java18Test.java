package cz.cacek.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://www.infoworld.com/article/3630510/jdk-18-the-new-features-in-java-18.html">infoworld article</a>.
 */
public class Java18Test {

    // https://openjdk.org/jeps/421 Deprecate Finalization for Removal

    @Test
    // https://openjdk.org/jeps/400
    public void utf8ByDefault() throws Exception {
        assertEquals(UTF_8, Charset.defaultCharset());
        assertThrows(UnsupportedCharsetException.class, () -> Charset.forName("default"));
    }
}
