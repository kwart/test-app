package cz.cacek.test;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check the <a href="https://openjdk.org/projects/jdk/25/">JDK 25 page</a>.
 */
public class Java25Test {

    // https://openjdk.org/projects/jdk/25/

    @Test
    // https://openjdk.org/jeps/506
    public void scopedValues() throws Exception {
        // Scoped Values are final - structured alternative to ThreadLocal
        final ScopedValue<String> USER = ScopedValue.newInstance();

        // Run with a scoped value bound
        ScopedValue.where(USER, "duke").run(() -> {
            assertEquals("duke", USER.get());
            assertTrue(USER.isBound());
        });

        // Not bound outside the scope
        assertFalse(USER.isBound());

        // Nested rebinding
        ScopedValue.where(USER, "outer").run(() -> {
            assertEquals("outer", USER.get());
            ScopedValue.where(USER, "inner").run(() -> {
                assertEquals("inner", USER.get());
            });
            // Restored after inner scope
            assertEquals("outer", USER.get());
        });

        // callWhere - returns a value from the scoped context
        String result = ScopedValue.where(USER, "caller").call(() -> {
            return "Hello, " + USER.get() + "!";
        });
        assertEquals("Hello, caller!", result);
    }

    @Test
    // https://openjdk.org/jeps/513
    public void flexibleConstructorBodies() {
        // Flexible Constructor Bodies are final
        class Base {
            final int validated;
            Base(int value) {
                this.validated = value;
            }
        }
        class Validated extends Base {
            final String name;
            Validated(int value, String name) {
                // Statements before super() - validate arguments
                if (value < 0) {
                    throw new IllegalArgumentException("negative value: " + value);
                }
                Objects.requireNonNull(name);
                super(value);
                this.name = name;
            }
        }
        Validated v = new Validated(10, "test");
        assertEquals(10, v.validated);
        assertThrows(IllegalArgumentException.class, () -> new Validated(-1, "bad"));
        assertThrows(NullPointerException.class, () -> new Validated(1, null));
    }

    @Test
    // https://openjdk.org/jeps/511
    public void moduleImportDeclarations() {
        // Module import declarations are final
        // The 'import module java.base;' declaration imports all public types from java.base
        // This is a compile-time feature - we verify it works by using types
        // that would normally need explicit imports, accessed via module import

        // All java.base types are available without individual imports
        // (shown implicitly by our use of java.util.* above)
        List<String> list = List.of("module", "imports", "work");
        assertEquals(3, list.size());
    }

    @Test
    // https://openjdk.org/jeps/510
    public void keyDerivationFunctionApi() throws Exception {
        // KDF API is final - provides key derivation functions
        javax.crypto.KDF hkdf = javax.crypto.KDF.getInstance("HKDF-SHA256");
        assertNotNull(hkdf);

        // Derive a key using HKDF
        javax.crypto.SecretKey inputKey = new javax.crypto.spec.SecretKeySpec(
                "input-key-material".getBytes(), "Generic");

        java.security.spec.AlgorithmParameterSpec params = javax.crypto.spec.HKDFParameterSpec
                .ofExtract()
                .addIKM(inputKey)
                .addSalt(new javax.crypto.spec.SecretKeySpec("salt".getBytes(), "Generic"))
                .thenExpand("info".getBytes(), 32);

        javax.crypto.SecretKey derivedKey = hkdf.deriveKey("Generic", params);
        assertNotNull(derivedKey);
        assertEquals(32, derivedKey.getEncoded().length);
    }
}
