package cz.cacek.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
import static javax.net.ssl.SSLEngineResult.Status.BUFFER_OVERFLOW;
import static javax.net.ssl.SSLEngineResult.Status.BUFFER_UNDERFLOW;
import static javax.net.ssl.SSLEngineResult.Status.CLOSED;
import static javax.net.ssl.SSLEngineResult.Status.OK;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;

public class TLSHandshaker implements Runnable {

    final Logger logger = Logger.getLogger(getClass().getName());

    final SSLEngine sslEngine;
    final ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
    final TLSExecutor tlsExecutor;

    final ByteBuffer decoderAppBuffer;
    final ByteBuffer decoderSrc;

    final ByteBuffer encoderSrcBuffer;
    final ByteBuffer encoderDst;

    public TLSHandshaker(SSLEngine sslEngine, TLSExecutor tlsExecutor) {
        this.sslEngine = sslEngine;
        this.tlsExecutor = tlsExecutor;

        this.decoderAppBuffer = ByteBuffer.allocate(sslEngine.getSession().getApplicationBufferSize());
        this.decoderSrc = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());

        encoderDst = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
        upcast(encoderDst).flip();
        if (sslEngine.getUseClientMode()) {
            encoderSrcBuffer = ByteBuffer.allocate(3);
            encoderSrcBuffer.put("HZC".getBytes(UTF_8));
            upcast(encoderSrcBuffer).flip();
        } else {
            encoderSrcBuffer = ByteBuffer.allocate(0);
        }
    }

    public void handle() throws Exception {
        compactOrClear(encoderDst);
        upcast(decoderSrc).flip();
        try {
            for (;;) {
                SSLEngineResult.HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
                switch (handshakeStatus) {
                    case FINISHED:
                        break;
                    case NEED_TASK:
                        tlsExecutor.executeHandshakeTasks(sslEngine);
                        return;
                    case NEED_WRAP:
                        SSLEngineResult wrapResult = sslEngine.wrap(encoderSrcBuffer, encoderDst);
                        SSLEngineResult.Status wrapResultStatus = wrapResult.getStatus();
                        if (wrapResultStatus == OK) {
                            // the wrap was a success, return to the loop to check the
                            // handshake status again.
                            continue;
                        } else if (wrapResultStatus == BUFFER_OVERFLOW) {
                            // not enough space available to write the content
                            return;
                        } else {
                            throw new IllegalStateException("Unexpected wrapResult:" + wrapResult);
                        }
                    case NEED_UNWRAP:
                        SSLEngineResult unwrapResult = sslEngine.unwrap(decoderSrc, decoderAppBuffer);
                        SSLEngineResult.Status unwrapStatus = unwrapResult.getStatus();
                        if (unwrapStatus == OK) {
                            // unwrapping was a success.
                            // go back to the for loop and check handshake status again
                            continue;
                        } else if (unwrapStatus == CLOSED) {
                            // the SSLEngine is closed. So lets return, since nothing
                            // can be done here. Eventually the channel will get closed
                            return;
                        } else if (unwrapStatus == BUFFER_UNDERFLOW) {
                            if (sslEngine.getHandshakeStatus() == NOT_HANDSHAKING) {
                                System.err.println("BUFFER_UNDERFLOW and NOT_HANDSHAKING");
                                // needed because of OpenSSL. OpenSSL can indicate buffer underflow,
                                // but handshake can complete at the same time. Check causes the loop
                                // to be retried.
                                continue;
                            }

                            // not enough data is available to decode the content, so lets return
                            // and wait for more data to be received.
                            return;
                        } else {
                            throw new IllegalStateException("Unexpected " + unwrapResult);
                        }
                    case NOT_HANDSHAKING:
                        // TLSv1.3 allows the appdata to be sent within the handshake messages already.
                        // These could be for instance the protocol header bytes ("HZC", "CP2", ...)
                        // Let's place such data directly to the TLSDecoder's dst buffer.
                        if (decoderAppBuffer.position() != 0) {
                            upcast(decoderAppBuffer).flip();
                            logger.info("App buffer contains some bytes: " + UTF_8.decode(decoderAppBuffer).toString());
                        }

                        // the src buffer could contain unencrypted data not needed for the handshake
                        // this data needs to be pushed to the TLSDecoder.
                        if (decoderSrc.hasRemaining()) {
                            logger.info("src has remaining bytes: " + decoderSrc.remaining());
                        }
                        // wakeup the outbound pipeline to complete the handshake.
                        return;
                    default:
                        throw new IllegalStateException();
                }
            }
        } finally {
            compactOrClear(decoderSrc);
            upcast(encoderDst).flip();
        }
    }

    public static Buffer upcast(Buffer buf) {
        return buf;
    }

    public static void compactOrClear(ByteBuffer bb) {
        if (bb.hasRemaining()) {
            bb.compact();
        } else {
            upcast(bb).clear();
        }
    }

    @Override
    public void run() {
        try {
            handle();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.info((sslEngine.getUseClientMode() ? "Client" : "Server") + " handshake=" + sslEngine.getHandshakeStatus());
        }
    }

}
