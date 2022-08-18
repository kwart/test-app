package cz.cacek.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.NamedParameterSpec;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://www.azul.com/blog/jdk-15-release-64-new-features-and-apis/">azul blog</a>.
 */
public class Java15Test {
    // JEP 372: Remove the Nashorn Scripting Engine.
    // JEP 374: Disable and deprecate biased locking.
    // JEP 377: ZGC: A Scalable Low-Latency Garbage Collector (Production; use -XX:+UseZGC)
    // JEP 379: Shenandoah: A Low-Pause-Time Garbage Collector. (from experimental to product feature; use -XX:+UseShenandoahGC)

    @Test
    // https://openjdk.org/jeps/378
    public void textBlocks() throws Exception {
        String html = """
                <html>
                    <body>
                        <p>Hello, world</p>
                    </body>
                </html>
                """;
        assertThat(html, startsWith("<html>"));
        assertThat(html, containsString("\n</html>"));

        String query = """
                SELECT "EMP_ID", "LAST_NAME" FROM "EMPLOYEE_TB"
                WHERE "CITY" = 'INDIANAPOLIS'
                ORDER BY "EMP_ID", "LAST_NAME";
                """;
        assertThat(query, containsString("\"CITY\" = 'INDIANAPOLIS'\n"));

        String html2 = """
                      <html>
                          <body>
                              <p>Hello, world</p>
                          </body>
                      </html>
                """;
        assertThat(html2, endsWith("\n      </html>\n"));

        assertThat("""
                line 1
                line 2
                line 3""", endsWith("line 3"));
    }

    @Test
    // JEP 339: Edwards-Curve Digital Signature Algorithm (EdDSA)
    public void eddsa() throws Exception {
        // example: generate a key pair and sign
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();
        // algorithm is pure Ed25519
        Signature sig = Signature.getInstance("Ed25519");
        sig.initSign(kp.getPrivate());
        sig.update("Ahoj".getBytes());
        byte[] s = sig.sign();
        assertEquals(64, s.length);

        // example: use KeyFactory to contruct a public key
        KeyFactory kf = KeyFactory.getInstance("EdDSA");
        boolean xOdd = true;
        BigInteger y = new BigInteger("123456789");
        NamedParameterSpec paramSpec = new NamedParameterSpec("Ed25519");
        EdECPublicKeySpec pubSpec = new EdECPublicKeySpec(paramSpec, new EdECPoint(xOdd, y));
        PublicKey pubKey = kf.generatePublic(pubSpec);
        assertEquals("X.509", pubKey.getFormat());
        assertEquals(44, pubKey.getEncoded().length);
    }
}
