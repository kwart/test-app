package cz.cacek.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.RecordComponent;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://www.azul.com/blog/jdk-15-release-64-new-features-and-apis/">azul blog</a>.
 */
public class Java16Test {

    // https://openjdk.org/jeps/338 Vector API (Incubator)
    // https://openjdk.org/jeps/393 Foreign Memory Access API (Incubator)

    // https://openjdk.org/jeps/376 ZGC: Concurrent Thread-Stack Processing (moved from safepoints to a concurrent phase,
    // allowing significantly reduced pauses inside GC safepoints, even on large heaps)

    // https://openjdk.org/jeps/387 Elastic Metaspace (memory can be returned to the operating system more promptly)

    // https://openjdk.org/jeps/386 Alpine Linux Port

    // JEP 357 and JEP 369 Move OpenJDK sources from Mercurial to GitHub
    // JEP 396: Strongly Encapsulate JDK Internals by Default

    String s = "just field";

    @Test
    // https://openjdk.org/jeps/394
    public void patternMatchingForInstanceOf() throws Exception {
        Object obj = "Test JDK 16";
        if (obj instanceof String s && s.length() > 5) {
            assertThat(s, containsString("JDK"));
        } else {
            fail();
        }
    }

    @Test
    // https://openjdk.org/jeps/395
    public void records() throws Exception {
        Point point = new Point(0, 0);
        Rectangle rect = new Rectangle(point, new Point(1, 2));
        assertEquals(point, rect.upperLeft);
        assertEquals(point, rect.upperLeft());
        assertEquals(new Point(1, 2), rect.lowerRight());

        // local record class
        record LocalPoint(int x, int y) {
        }

        // reflection API support
        assertTrue(LocalPoint.class.isRecord());
        assertTrue(Range.class.isRecord());
        RecordComponent[] rcs = LocalPoint.class.getRecordComponents();
        assertEquals(2, rcs.length);
        assertEquals("x", rcs[0].getName());
    }

    @Test
    // https://openjdk.org/jeps/380
    // https://www.baeldung.com/java-unix-domain-socket
    public void unixDomainSocketChannels() throws Exception {
        Path dockerSocketPath = Path.of("/run/docker.sock");
        assumeTrue(Files.exists(dockerSocketPath));
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(dockerSocketPath);
        SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        channel.connect(socketAddress);
        ByteBuffer buffer = ByteBuffer.wrap("""
                GET /version HTTP/1.0
                Host: localhost
                Accept: */*\n
                """.getBytes(UTF_8));
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }

        ByteBuffer readBuf = ByteBuffer.allocate(8 * 1024);
        while (channel.read(readBuf) >= 0 && readBuf.hasRemaining()) {
        }
        String response = new String(readBuf.array(), UTF_8);
        assertThat(response, containsString("Engine"));
    }

    @Test
    public void streamToListTerminalOperation() throws Exception {
        List<String> listFromStream = Stream.of("a", "B", "CCC").toList();
        assertEquals(Arrays.asList("a", "B", "CCC"), listFromStream);
        // unmodifiable list
        assertThrows(UnsupportedOperationException.class, () -> listFromStream.add("d"));
    }

    @Test
    public void streamMapMultiIntermediateOperation() throws Exception {
        // For a stream of words, the result will be list containing the original each included n-times where n==word.length()
        var result = Stream.of("", "ahoj", "Bye", "Dobry den").mapMulti((str, consumer) -> {
            for (int i = 0; i < str.length(); i++)
                consumer.accept(str);
        }).toList();
        assertThat(result, hasSize("ahojByeDobry den".length()));
        assertThat(result, containsInRelativeOrder("ahoj", "Bye", "Dobry den"));
        assertThat(result, not(containsInRelativeOrder("")));
    }

    @Test
    // https://bugs.openjdk.org/browse/JDK-8159746
    public void invokeInterfaceDefaultMethodsFromProxy() throws Exception {
        AnInterface proxy = (AnInterface) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class<?>[] { AnInterface.class }, (prox, method, args) -> {
                    if (method.isDefault()) {
                        return InvocationHandler.invokeDefault(prox, method, args) + " nice";
                    }
                    return null;
                });
        assertEquals("Returning something nice", proxy.withDefaultMethod());
    }

    // A record class declaration does not have an extends clause. The superclass of a record class is always java.lang.Record
    // A record class is implicitly final, and cannot be abstract.
    // The fields derived from the record components are final.
    // A record class cannot explicitly declare instance fields, and cannot contain instance initializers.
    // A record class cannot declare native methods.
    public record Point(int x, int y) {
    }

    record Rectangle(Point upperLeft, Point lowerRight) {
    };

    record Range(int lo, int hi) {
        /**
         * Compact canonical constructor that validates its implicit formal parameters.
         *
         * @param lo
         * @param hi
         */
        Range {
            if (lo > hi) // referring here to the implicit constructor parameters
                throw new IllegalArgumentException(String.format("(%d,%d)", lo, hi));
        }
    }

    interface AnInterface {
        default String withDefaultMethod() {
            return "Returning something";
        }
    }
}
