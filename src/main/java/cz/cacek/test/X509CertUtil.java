package cz.cacek.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.IOUtil;
import com.hazelcast.util.StringUtil;

public class X509CertUtil {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(X509CertUtil.class.getName());

    // https://tools.ietf.org/html/rfc7468#section-5
    private static final String PEM_CERT_BEGIN = "-----BEGIN CERTIFICATE-----";
    private static final String PEM_CERT_END = "-----END CERTIFICATE-----";


    public static KeyStore asTruststore(Certificate[] certs) throws GeneralSecurityException, IOException {
        if (certs == null) {
            return null;
        }
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        for (int i = 0; i < certs.length; i++) {
            ks.setCertificateEntry("cert-" + i, certs[i]);
        }
        return ks;
    }

    public static Certificate[] parsePemCertificates(String pemCertsCollection) {
        if (pemCertsCollection == null) {
            return null;
        }
        int pos = -1;
        ArrayList<Certificate> certs = new ArrayList<Certificate>();
        while ((pos = pemCertsCollection.indexOf(PEM_CERT_BEGIN, pos + 1)) > -1) {
            int endPos = pemCertsCollection.indexOf(PEM_CERT_END, pos + PEM_CERT_BEGIN.length());
            if (endPos > -1) {
                String certPem = pemCertsCollection.substring(pos, endPos + PEM_CERT_END.length());
                try {
                    certs.add(generateCertificate(certPem));
                } catch (CertificateException e) {
                    LOGGER.warning("Unable to generate certificate from PEM\n" + certPem);
                }
            } else {
                LOGGER.warning("Unmatched " + PEM_CERT_BEGIN + " provided in the PEM certificates collection.");
            }
        }
        return certs.toArray(new Certificate[certs.size()]);
    }

    public static Certificate generateCertificate(String pemCert) throws CertificateException {
        return generateCertificate(pemCert.getBytes(StringUtil.UTF8_CHARSET));
    }

    public static Certificate generateCertificate(byte[] certBytes) throws CertificateException {
        if (certBytes == null) {
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCertificate(bais);
        } finally {
            IOUtil.closeResource(bais);
        }
    }
}
