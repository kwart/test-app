package cz.cacek.test;

import static cz.cacek.test.TLSHandshaker.copyBuffers;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;

import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

public class App {

    public static final String PROTOCOL = "TLSv1.3";

    final Logger logger = Logger.getLogger(getClass().getName());

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF-%1$tT [%4$s] %2$s %5$s%6$s%n");
        App app = new App();
        for (int i = 0; i < 10; i++) {
            app.run();
        }
        System.out.println("******* Sleeping 1s before GC");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("******* Running GC");
        System.gc();
        System.out.println("******* Waiting for debug/heapdump/etc before the application finishes");
        TimeUnit.MINUTES.sleep(60);
    }

    public void run() {
        try {
            System.out.println("******* Starting handshake");
            TLS client = new TLS(createSslEngine(true, PROTOCOL));
            TLS server = new TLS(createSslEngine(false, PROTOCOL));
            long timeout = System.currentTimeMillis() + 3000L;
            while (handshaking(server) || handshaking(client)) {
                client.handshaker.run();
                copyBuffers(client.handshaker.encoderDst, server.handshaker.decoderSrc);

                server.handshaker.run();
                copyBuffers(server.handshaker.encoderDst, client.handshaker.decoderSrc);
                if (System.currentTimeMillis() > timeout) {
                    throw new IllegalStateException("We're out of luck. The handshake times out.");
                }
                Thread.sleep(10);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Handshake failed", e);
        } finally {
            logger.info("Handshake finished");
            System.out.println("******* Finished handshake");
        }
    }

    private boolean handshaking(TLS tls) {
        HandshakeStatus handshakeStatus = tls.engine.getHandshakeStatus();
        return handshakeStatus != NOT_HANDSHAKING;
    }

    private SSLEngine createSslEngine(boolean clientMode, String protocol) throws SSLException {
        SslContext context = createSslContext(clientMode);
        SSLEngine engine = context.newEngine(UnpooledByteBufAllocator.DEFAULT);
        engine.setEnabledProtocols(new String[] { protocol });
        engine.beginHandshake();
        return engine;
    }

    private SslContext createSslContext(boolean clientMode) throws SSLException {
        SslContextBuilder builder;
        File certChain = new File("localhost-cert.pem");
        File key = new File("localhost-key.pem");
        String keyPassword = null;
        if (clientMode) {
            builder = SslContextBuilder.forClient().keyManager(certChain, key, keyPassword);
        } else {
            builder = SslContextBuilder.forServer(certChain, key, keyPassword);
            builder.clientAuth(ClientAuth.REQUIRE);
        }
        builder.trustManager(new File("ca-cert.pem"));
        builder.sslProvider(SslProvider.OPENSSL);
        return builder.build();
    }

    public static class TLS {
        final SSLEngine engine;
        final TLSHandshaker handshaker;

        public TLS(SSLEngine engine) {
            this.engine = engine;
            this.handshaker = new TLSHandshaker(engine);
        }

    }
}
