package cz.cacek.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://www.azul.com/blog/81-new-features-and-apis-in-jdk-13/">azul blog</a>.
 */
public class Java13Test {

    @Test
    // https://openjdk.org/jeps/355
    @Disabled("Eclipse says Java 15.")
    public void textBlocksPreview() throws Exception {
        // String html = """
        // <html>
        // <body>
        // <p>Hello, world</p>
        // </body>
        // </html>
        // """;
    }

    // JEP 350: Dynamic CDS Archive.
    // JEP 351: ZGC: Uncommit unused memory.
    // JEP 353: Reimplement the legacy Socket API

    @Test
    public void newFileSystem() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        Path testZip = Paths.get("target", "test.zip");
        Files.deleteIfExists(testZip);
        try (FileSystem zipfs = FileSystems.newFileSystem(testZip, env)) {
            Files.copy(Paths.get("pom.xml"), zipfs.getPath("/foo.xml"), StandardCopyOption.REPLACE_EXISTING);
        }
        assertTrue(Files.exists(testZip));
        try (FileSystem zipfs = FileSystems.newFileSystem(testZip)) {
            String str = Files.readString(zipfs.getPath("/foo.xml"));
            assertTrue(str.contains("java13"));
        }
    }

    // javax.crypto – support for MS Cryptography Next Generation (CNG)
}
