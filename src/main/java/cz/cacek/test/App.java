package cz.cacek.test;

import static io.netty.handler.ssl.SslProvider.OPENSSL;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;

import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class App {

    final Logger logger = Logger.getLogger(getClass().getName());

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF-%1$tT [%4$s] %2$s %5$s%6$s%n");
        App app = new App();
        // interestingly, if I run with the blocking executor first, the subsequent non-blocking executor scenario finishes
        // properly
        // app.run(true);
        app.run(false);
    }

    public void run(boolean useBlockingExecutor) {
        try (TLS client = new TLS(createSslEngine(true), createExecutor(useBlockingExecutor));
                TLS server = new TLS(createSslEngine(false), createExecutor(useBlockingExecutor))) {
            long timeout = System.currentTimeMillis() + 3000L;
            while (handshaking(server) || handshaking(client)) {
                client.handshaker.run();
                server.handshaker.decoderSrc.put(client.handshaker.encoderDst);

                server.handshaker.run();
                client.handshaker.decoderSrc.put(server.handshaker.encoderDst);
                if (System.currentTimeMillis() > timeout) {
                    throw new IllegalStateException("We're out of luck. The handshake times out.");
                }
                //give other threads more chances to finish
                Thread.sleep(10);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Handshake failed", e);
            throw new RuntimeException(e);
        } finally {
            logger.info("Handshake finished");
            System.out.println("***************************************************************************************");
            System.out.println("**********   FINISHED WITH " + (useBlockingExecutor ? "" : "NON-") + "BLOCKING EXECUTOR");
            System.out.println("***************************************************************************************");
        }
    }

    private TLSExecutor createExecutor(boolean blocking) {
        return blocking ? new TLSExecutorBlocking() : new TLSExecutorNonBlocking();
    }

    private boolean handshaking(TLS tls) {
        HandshakeStatus handshakeStatus = tls.engine.getHandshakeStatus();
        return handshakeStatus != NOT_HANDSHAKING;
    }

    private SSLEngine createSslEngine(boolean clientMode) throws SSLException {
        SslContext context = createSslContext(clientMode);
        SSLEngine engine = context.newEngine(UnpooledByteBufAllocator.DEFAULT);
        engine.setEnabledProtocols(new String[] { "TLSv1.3" });
        engine.beginHandshake();
        return engine;
    }

    private SslContext createSslContext(boolean clientMode) throws SSLException {
        SslContextBuilder builder;
        File certChain = new File("src/main/resources/server.crt");
        File key = new File("src/main/resources/server.pem");
        String keyPassword = null;
        if (clientMode) {
            builder = SslContextBuilder.forClient();
            builder.keyManager(certChain, key, keyPassword);
        } else {
            builder = SslContextBuilder.forServer(certChain, key, keyPassword);
            builder.clientAuth(ClientAuth.NONE);
        }
        builder.trustManager(new File("src/main/resources/all.crt"));
        builder.sslProvider(OPENSSL);
        return builder.build();
    }

    public static class TLS implements Closeable {
        final SSLEngine engine;
        final TLSExecutor tlsExecutor;
        final TLSHandshaker handshaker;

        public TLS(SSLEngine engine, TLSExecutor tlsExecutor) {
            this.engine = engine;
            this.tlsExecutor = tlsExecutor;
            this.handshaker = new TLSHandshaker(engine, tlsExecutor);
        }

        @Override
        public void close() throws IOException {
            tlsExecutor.shutdown();
        }
    }
}
