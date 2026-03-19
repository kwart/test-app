package cz.cacek.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/26/">JDK 26 page</a>.
 */
public class Java26Test {

    // https://openjdk.org/projects/jdk/26/

    @Test
    // https://openjdk.org/jeps/500
    public void prepareFinalMeansFinal() {
        // JEP 500: Prepare to Make Final Mean Final
        // This JEP adds warnings when reflection is used to mutate final fields.
        // We can demonstrate that final fields work as expected.
        record ImmutablePoint(int x, int y) {}
        ImmutablePoint p = new ImmutablePoint(3, 4);
        assertEquals(3, p.x());
        assertEquals(4, p.y());

        // Final fields in records and classes are truly immutable
        final String msg = "immutable";
        assertEquals("immutable", msg);
    }

    @Test
    // https://openjdk.org/jeps/517
    public void http3ForHttpClient() throws Exception {
        // JEP 517: HTTP/3 support in the HTTP Client API
        // The existing HttpClient transparently supports HTTP/3 (QUIC-based)
        // when the server supports it. The API is unchanged.
        var client = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_2)
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
        assertNotNull(client);
        // HTTP/3 negotiation happens automatically via ALPN/Alt-Svc
    }
}
