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
 * Check the
 * <a href="https://www.azul.com/resources-hub/devops/azul-webinar-jdk-17-long-term-support-and-great-new-features">azul
 * webinar</a>.
 */
public class Java17Test {
    @Test
    public void test() throws Exception {
    }
}
