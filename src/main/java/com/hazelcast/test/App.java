package com.hazelcast.test;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;

import com.hazelcast.client.impl.protocol.AuthenticationStatus;
import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.ClientAuthenticationCodec;
import com.hazelcast.client.impl.protocol.constants.ResponseMessageConst;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.IOUtil;

/**
 * @author Josef Cacek
 */
public class App {

    public static final byte[] AUTHN_REQUEST = createRequest();

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        public HazelcastInstance hz;

        @Setup(Level.Trial)
        public void setUp() {
            System.setProperty("hazelcast.logging.type", "log4j2");
            Config config = new Config();
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getSecurityConfig().setEnabled(true);
            hz = Hazelcast.newHazelcastInstance(config);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            hz.getLifecycleService().terminate();
        }

    }

    @Benchmark
    @Threads(3)
    public static void authenticate(ExecutionPlan plan) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", 5701)) {
            // socket.setSoTimeout(5000);
            try (OutputStream os = socket.getOutputStream(); InputStream is = socket.getInputStream()) {
                os.write("CB2".getBytes(US_ASCII));
                os.write(AUTHN_REQUEST);
                readResponse(is);
            }
        }
    }

    private static void readResponse(InputStream is) throws IOException {
        ByteBuffer responseBuffer = ByteBuffer.allocate(23);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        IOUtil.readFully(is, responseBuffer.array());
        // frame size
        responseBuffer.getInt();
        // version
        responseBuffer.get();
        // Flags
        responseBuffer.get();
        // Type - Generic.AuthenticationResponse
        short responseType = responseBuffer.getShort();
        if (responseType != ResponseMessageConst.AUTHENTICATION) {
            throw new IllegalStateException("Wrong response type: " + responseType);
        }
        // Correlation ID
        responseBuffer.getLong();
        // Partition ID (-1)
        responseBuffer.getInt();
        // Data Offset
        responseBuffer.getShort();

        // Authentication status
        byte authnStatus = responseBuffer.get();
        if (authnStatus != AuthenticationStatus.AUTHENTICATED.getId()) {
            throw new IllegalStateException("Wrong authentication status: " + authnStatus);
        }
    }

    private static ClientMessage createAuthenticationMessage() {
        return ClientAuthenticationCodec.encodeRequest("dev", "dev-pass", null, null, true, "xxx", (byte) 1);
    }

    private static byte[] createRequest() {
        ByteBuffer messageBuffer = ByteBuffer.allocateDirect(1000);
        messageBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ClientMessage msg = createAuthenticationMessage().addFlag(ClientMessage.BEGIN_AND_END_FLAGS);
        msg.writeTo(messageBuffer);
        return byteBufferToBytes(messageBuffer);
    }

    private static byte[] byteBufferToBytes(ByteBuffer buffer) {
        buffer.flip();
        byte[] requestBytes = new byte[buffer.limit()];
        buffer.get(requestBytes);
        return requestBytes;
    }
}
