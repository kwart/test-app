package cz.cacek.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.client.impl.ClientEngine;
import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.ClientAuthenticationCodec;
import com.hazelcast.client.impl.protocol.codec.MapPutCodec;
import com.hazelcast.client.impl.protocol.task.AuthenticationBaseMessageTask;
import com.hazelcast.cluster.Address;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.EndpointQualifier;
import com.hazelcast.instance.impl.HazelcastInstanceImpl;
import com.hazelcast.instance.impl.HazelcastInstanceProxy;
import com.hazelcast.internal.networking.ChannelOptions;
import com.hazelcast.internal.networking.InboundPipeline;
import com.hazelcast.internal.networking.OutboundFrame;
import com.hazelcast.internal.networking.OutboundPipeline;
import com.hazelcast.internal.networking.nio.AbstractChannel;
import com.hazelcast.internal.nio.Connection;
import com.hazelcast.internal.nio.ConnectionLifecycleListener;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.serialization.impl.HeapData;
import com.hazelcast.internal.server.ServerConnectionManager;
import com.hazelcast.internal.server.tcp.TcpServerConnection;
import com.hazelcast.internal.server.tcp.TcpServerConnectionManager;
import com.hazelcast.partition.PartitionService;

/**
 * The App!
 */
public class LiteMemberApp {

//    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("com.hazelcast");
//    static {
//        java.util.logging.ConsoleHandler ch = new java.util.logging.ConsoleHandler();
//        ch.setLevel(java.util.logging.Level.ALL);
//        logger.addHandler(ch);
//        logger.setLevel(java.util.logging.Level.ALL);
//    }

    public static void main(String[] args) throws Exception {
        Config config = new Config().setLiteMember(true);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        HazelcastInstanceImpl hzOrig = ((HazelcastInstanceProxy) hz).getOriginal();
        TcpServerConnectionManager cm = (TcpServerConnectionManager) hzOrig.node.server
                .getConnectionManager(EndpointQualifier.CLIENT);
        PartitionService ps = hzOrig.getPartitionService();
        TcpServerConnection connection = new TcpServerConnection(cm, new ConnectionLifecycleListener<TcpServerConnection>() {

            @Override
            public void onConnectionClose(TcpServerConnection connection, Throwable t, boolean silent) {
                if (t != null) {
                    t.printStackTrace();
                }
            }
        }, 0, new MyChannel(), true);
        ClientEngine clientEngine = getClientEngine(hz);
        InternalSerializationService ss = getSerializationService(hz);
        UUID uuid = UUID.randomUUID();
        ClientMessage authn = ClientAuthenticationCodec.encodeRequest("dev", null, null, uuid, "test", ss.getVersion(),"xx", "client",
                Collections.emptyList());
        authn.setConnection(connection);
        clientEngine.accept(authn);
        Data keyData = ss.toData("key");
        ClientMessage message = MapPutCodec.encodeRequest("test", keyData, ss.toData("value"), 0, 0);
        message.setConnection(connection);
        message.setPartitionId(ps.getPartition(keyData).getPartitionId());
        clientEngine.accept(message);
        System.out.println(hz.getMap("test").get("key"));
    }

    private static InternalSerializationService getSerializationService(HazelcastInstance hz) {
        return ((HazelcastInstanceProxy) hz).getSerializationService();
    }

    private static ClientEngine getClientEngine(HazelcastInstance hz) {
        return ((HazelcastInstanceProxy) hz).getOriginal().node.clientEngine;
    }

    static class MyChannel extends AbstractChannel {

        @Override
        public SocketAddress remoteSocketAddress() {
            return new InetSocketAddress("1.1.1.1", 5555);
        }

        @Override
        public SocketAddress localSocketAddress() {
            return new InetSocketAddress("1.1.1.1", 5556);
        }

        public MyChannel() {
            super(null, false);
        }

        @Override
        public ChannelOptions options() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InboundPipeline inboundPipeline() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public OutboundPipeline outboundPipeline() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long lastReadTimeMillis() {
            return System.currentTimeMillis()-10;
        }

        @Override
        public long lastWriteTimeMillis() {
            return System.currentTimeMillis()-10;
        }

        @Override
        public void start() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean write(OutboundFrame frame) {
            System.out.println(frame);
            ClientMessage cm = (ClientMessage) frame;
            cm.getMessageType();
            return true;
        }

        @Override
        public long bytesRead() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long bytesWritten() {
            // TODO Auto-generated method stub
            return 0;
        }

    }
}
