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

    public static volatile boolean ALLOW_APP_BUFFER_RESIZE = false;

    final Logger logger = Logger.getLogger(getClass().getName());

    final SSLEngine sslEngine;
    final ByteBuffer emptyBuffer = ByteBuffer.allocate(0);

    volatile ByteBuffer decoderAppBuffer;
    final ByteBuffer decoderSrc;

    final ByteBuffer encoderSrcBuffer;
    final ByteBuffer encoderDst;

    public TLSHandshaker(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;

        int applicationBufferSize = sslEngine.getSession().getApplicationBufferSize();
        int packetBufferSize = sslEngine.getSession().getPacketBufferSize();
        logger.fine("SSLEngine session buffer sizes - application: " + applicationBufferSize + ", packet: " + packetBufferSize);
        this.decoderAppBuffer = ByteBuffer.allocate(applicationBufferSize);
        this.decoderSrc = ByteBuffer.allocate(packetBufferSize);

        encoderDst = ByteBuffer.allocate(packetBufferSize);
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
                logger.fine(
                        (sslEngine.getUseClientMode() ? "Client" : "Server") + " handshake=" + sslEngine.getHandshakeStatus());
                switch (handshakeStatus) {
                    case FINISHED:
                        break;
                    case NEED_TASK:
                        Runnable task;
                        while ((task = sslEngine.getDelegatedTask()) != null) {
                            task.run();
                        }
                        return;
                    case NEED_WRAP:
                        SSLEngineResult wrapResult = sslEngine.wrap(encoderSrcBuffer, encoderDst);
                        SSLEngineResult.Status wrapResultStatus = wrapResult.getStatus();
                        logger.fine("Wrap result status: " + wrapResultStatus);
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
                        logger.fine("Unwrap result status: " + unwrapStatus);
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
                                // handshake is done
                                continue;
                            }
                            return;
                        } else if (ALLOW_APP_BUFFER_RESIZE && unwrapStatus == BUFFER_OVERFLOW) {
                            logger.warning("Unexpected BUFFER_OVERFLOW after the unwrap");
                            int applicationBufferSize = sslEngine.getSession().getApplicationBufferSize();
                            int packetBufferSize = sslEngine.getSession().getPacketBufferSize();
                            logger.fine("SSLEngine session buffer sizes - application: " + applicationBufferSize + ", packet: "
                                    + packetBufferSize);
                            if (decoderAppBuffer.capacity() < applicationBufferSize) {
                                logger.warning("Resizing the appBuffer, the session size had changed!");
                                ByteBuffer tmpBuf = ByteBuffer.allocate(applicationBufferSize);
                                if (decoderAppBuffer.position() > 0) {
                                    decoderAppBuffer.flip();
                                    int count = copyBuffers(decoderAppBuffer, tmpBuf);
                                    logger.warning("Bytes transfered to new buffer: " + count);
                                }
                                decoderAppBuffer = tmpBuf;
                            }
                        } else {
                            throw new IllegalStateException("Unexpected " + unwrapResult);
                        }
                    case NOT_HANDSHAKING:
                        // TLSv1.3 allows the appdata to be sent within the handshake messages already.
                        if (decoderAppBuffer.position() != 0) {
                            upcast(decoderAppBuffer).flip();
                            logger.fine("App buffer contains some bytes: " + UTF_8.decode(decoderAppBuffer).toString());
                        }

                        // the src buffer could contain unencrypted data not needed for the handshake
                        // this data needs to be pushed to the TLSDecoder.
                        if (decoderSrc.hasRemaining()) {
                            logger.fine("src has remaining bytes: " + decoderSrc.remaining());
                        }
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

    public static int copyBuffers(ByteBuffer src, ByteBuffer dst) {
        int n = Math.min(src.remaining(), dst.remaining());
        int srcPosition = src.position();
        dst.put(src.array(), srcPosition, n);
        upcast(src).position(srcPosition + n);
        return n;
    }

    @Override
    public void run() {
        try {
            handle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
