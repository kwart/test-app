package com.hazelcast.test;

import static com.hazelcast.client.impl.protocol.ClientMessage.IS_FINAL_FLAG;
import static com.hazelcast.client.impl.protocol.ClientMessage.RESPONSE_BACKUP_ACKS_FIELD_OFFSET;
import static com.hazelcast.client.impl.protocol.ClientMessage.SIZE_OF_FRAME_LENGTH_AND_FLAGS;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.INT_SIZE_IN_BYTES;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.decodeByte;
import static com.hazelcast.internal.nio.IOUtil.readFully;
import static com.hazelcast.internal.nio.Protocols.CLIENT_BINARY;
import static com.hazelcast.internal.util.StringUtil.UTF8_CHARSET;
import static java.util.Collections.emptyList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

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
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.nio.Bits;

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
                os.write(CLIENT_BINARY.getBytes(UTF8_CHARSET));
                os.write(AUTHN_REQUEST);
                readResponse(is);
            }
        }
    }

    private static void readResponse(InputStream is) throws IOException {
        ByteBuffer frameSizeBuffer = ByteBuffer.allocate(SIZE_OF_FRAME_LENGTH_AND_FLAGS);
        frameSizeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        readFully(is, frameSizeBuffer.array());
        int frameSize = frameSizeBuffer.getInt();
//        int flags = frameSizeBuffer.getShort() & 0xffff;
        byte[] content = new byte[frameSize - SIZE_OF_FRAME_LENGTH_AND_FLAGS];
        readFully(is, content);
        int responseType = Bits.readIntL(content, ClientMessage.TYPE_FIELD_OFFSET);
        if (responseType != ClientAuthenticationCodec.RESPONSE_MESSAGE_TYPE) {
            throw new IllegalStateException("Wrong response type: " + responseType);
        }
        int statusIdx = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + INT_SIZE_IN_BYTES;
        byte authnStatus = decodeByte(content, statusIdx);
        if (authnStatus != AuthenticationStatus.AUTHENTICATED.getId()) {
            throw new IllegalStateException("Wrong authentication status: " + authnStatus);
        }
    }

    private static ClientMessage createAuthenticationMessage() {
        return ClientAuthenticationCodec.encodeRequest("dev", null, null, UUID.randomUUID(), "FOO",
                (byte) 1, "clientName", "xxx", emptyList(), -1, null);
    }

    private static byte[] createRequest() {
        ClientMessage msg = createAuthenticationMessage();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            for (ClientMessage.ForwardFrameIterator it = msg.frameIterator(); it.hasNext(); ) {
                ClientMessage.Frame frame = it.next();
                os.write(frameAsBytes(frame, !it.hasNext()));
            }
            os.flush();
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] byteBufferToBytes(ByteBuffer buffer) {
        buffer.flip();
        byte[] requestBytes = new byte[buffer.limit()];
        buffer.get(requestBytes);
        return requestBytes;
    }

    private static byte[] frameAsBytes(ClientMessage.Frame frame, boolean isLastFrame) {
        byte[] content = frame.content != null ? frame.content : new byte[0];
        int frameSize = content.length + SIZE_OF_FRAME_LENGTH_AND_FLAGS;
        ByteBuffer buffer = ByteBuffer.allocateDirect(frameSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(frameSize);
        if (!isLastFrame) {
            buffer.putShort((short) frame.flags);
        } else {
            buffer.putShort((short) (frame.flags | IS_FINAL_FLAG));
        }
        buffer.put(content);
        return byteBufferToBytes(buffer);
    }

}
