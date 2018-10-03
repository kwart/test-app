package cz.cacek.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import com.hazelcast.nio.IOUtil;
import com.hazelcast.util.StringUtil;

/**
 * Hazelcast Hello world!
 */
public class App {

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = App.class.getResourceAsStream("/testcert.crt");
        try {
            IOUtil.drainTo(is, baos);
        } finally {
            IOUtil.closeResource(is);
            IOUtil.closeResource(baos);
        }
        Certificate[] certs = X509CertUtil.parsePemCertificates(StringUtil.bytesToString(baos.toByteArray()));
        System.out.println(Arrays.toString(certs));
        if (certs.length != 2)
            throw new AssertionError("Wrong number of certificates");
        KeyStore ks = X509CertUtil.asTruststore(certs);
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            System.out.println(aliases.nextElement());
        }
        if (ks.containsAlias("cert-0") && ks.containsAlias("CERT-1")) {
            System.out.println("OK");
        } else {
            throw new AssertionError("Expected certificates not found in the truststore");
        }
    }
}
