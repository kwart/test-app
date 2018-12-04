package cz.cacek.test;

import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import org.junit.Test;

/**
 * A test template.
 */
public class MessageDigestTest {

    @Test
    public void testNullToMain() throws Exception {
        Provider[] providers = Security.getProviders();
        System.out.println("Provider 1: " + Security.getProperty("security.provider.1"));
        System.out.println("Providers (" + System.getProperty("java.home") + "):");
        for (Provider provider : providers) {
            System.out.println("\t" + provider.getName());
        }
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(new byte[0]);
        System.out.println("MD5 digest" + Arrays.toString(md5.digest()));
    }

}
