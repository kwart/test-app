package cz.cacek.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

/**
 * Check the <a href="https://www.infoworld.com/article/3630510/jdk-18-the-new-features-in-java-18.html">infoworld article</a>.
 */
public class Java18Test {

    // https://openjdk.org/projects/jdk/18/
    // https://openjdk.org/jeps/421 Deprecate Finalization for Removal

    @Test
    // https://openjdk.org/jeps/400
    public void utf8ByDefault() throws Exception {
        assertEquals(UTF_8, Charset.defaultCharset());
        assertThrows(UnsupportedCharsetException.class, () -> Charset.forName("default"));
    }

    @Test
    // https://openjdk.org/jeps/408
    public void simpleWebServer() throws Exception {
        HttpServer server = SimpleFileServer.createFileServer(new InetSocketAddress(18080), Path.of(".").toAbsolutePath(),
                SimpleFileServer.OutputLevel.VERBOSE);
        server.start();
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI("http://127.0.0.1:18080/pom.xml")).build();
            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("java18"));
        } finally {
            server.stop(0);
        }

        var handler = SimpleFileServer.createFileHandler(Path.of("target").toAbsolutePath());
    }
}
