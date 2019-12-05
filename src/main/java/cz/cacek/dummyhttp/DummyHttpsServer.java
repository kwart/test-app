package cz.cacek.dummyhttp;

import java.io.IOException;
import java.security.KeyStore;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Dummy HTTPs server which uses keypair located in {@code DummyHttpsServer.keystore}.
 * <p>
 * Sample usage:
 *
 * <pre>
 * DummyHttpsServer httpsServer = new DummyHttpsServer(8080, "Test".getBytes());
 * HttpsURLConnection.setDefaultSSLSocketFactory(httpsServer.getClientSocketFactory());
 * try {
 *     new Thread(httpsServer).start();
 *     httpsServer.waitForStart();
 *     URL url = httpsServer.getUrl();
 *     // use the server
 *     InputStream is = url.openConnection().getInputStream();
 * } finally {
 *     httpsServer.shutdown();
 * }
 * </pre>
 * @see DummyHttpServer
 */
public class DummyHttpsServer extends DummyHttpServer {

    private final SSLContext sslContext;
    private final SSLContext clientSslContext;

    public DummyHttpsServer(int port, byte[] content) {
        super(port, content);
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(getClass().getResourceAsStream("/expired.p12"), "123456".toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, "123456".toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), null, null);
            clientSslContext = SSLContext.getInstance("TLS");
            clientSslContext.init(null, tmf.getTrustManagers(), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProtocol() {
        return "https";
    }

    @Override
    protected ServerSocketFactory getServerSocketFactory() throws IOException {
        return sslContext.getServerSocketFactory();
    }

    @Override
    public SSLSocketFactory getClientSocketFactory() {
        return clientSslContext.getSocketFactory();
    }
}