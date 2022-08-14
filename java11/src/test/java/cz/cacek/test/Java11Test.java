package cz.cacek.test;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://www.azul.com/blog/90-new-features-and-apis-in-jdk-11/">azul blog</a>.
 */
public class Java11Test {

    @Test
    public void testLocalVariableForLambda() throws Exception {
        var list = Optional.of("Ahoj").stream().map((@SuppressWarnings("whatever") var s) -> s.toLowerCase())
                .collect(Collectors.toList());
        assertEquals(List.of("ahoj"), list);
    }

    @Test
    @Disabled("Just to build faster :)")
    public void httpClientApi() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(new URI("https://postman-echo.com/post"))
                .POST(HttpRequest.BodyPublishers.ofString("JEP321")).headers("Content-Type", "text/plain").build();

        HttpResponse<String> response = HttpClient.newBuilder().proxy(ProxySelector.getDefault()).build().send(request,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertTrue(response.body().contains("JEP321"));
    }

    @Test
    public void apiAdditions() throws Exception {
        var pomxml = Files.readString(Path.of("pom.xml"));
        assertTrue(pomxml.contains(">java11<"));
        var path = Files.writeString(Path.of("target", "writeStringTest"), "Supr", CREATE, TRUNCATE_EXISTING);
        assertTrue(Files.readString(path).equals("Supr"));

        // TimeUnit.convert
        Duration duration = Duration.ofMinutes(135);
        assertEquals(2, TimeUnit.HOURS.convert(duration));

        // Predicate.not, Pattern.asMatchPredicate
        Pattern pattern = Pattern.compile(".*artifact.*");
        Files.readAllLines(Path.of("pom.xml")).stream().filter(Predicate.not(String::isBlank))
                .filter(pattern.asMatchPredicate()).forEach(s -> assertTrue(!s.isBlank()));
    }

    @Test
    // https://openjdk.org/jeps/329
    public void cryptoAdditions() throws Exception {
        // Get a Cipher instance and set up the parameters
        // Assume SecretKey "key", 12-byte nonce "nonceBytes" and plaintext "pText"
        // are coming from outside this code snippet
        Cipher mambo = Cipher.getInstance("ChaCha20");
        ChaCha20ParameterSpec mamboSpec = new ChaCha20ParameterSpec("nonceBytes12".getBytes(), 7); // Use a starting counter
                                                                                                   // value of "7"
        // Encrypt our input
        byte[] keyBytes = new byte[256 / 8];
        // SecureRandom secureRandom = new SecureRandom();
        // secureRandom.nextBytes(keyBytes);
        mambo.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "ChaCha20"), mamboSpec);
        byte[] encryptedResult = mambo.doFinal("pText".getBytes());
        assertArrayEquals(new byte[] { 109, -31, -119, 107, 108 }, encryptedResult);
    }
}
