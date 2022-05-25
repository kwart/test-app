package cz.cacek.test;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.hazelcast.client.impl.ClientEngine;
import com.hazelcast.client.impl.protocol.AuthenticationStatus;
import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.ClientAuthenticationCodec;
import com.hazelcast.client.impl.protocol.codec.ClientAuthenticationCodec.ResponseParameters;
import com.hazelcast.client.impl.protocol.codec.MapGetCodec;
import com.hazelcast.client.impl.protocol.codec.MapPutCodec;
import com.hazelcast.client.impl.protocol.codec.MapRemoveCodec;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.EndpointQualifier;
import com.hazelcast.instance.impl.HazelcastInstanceImpl;
import com.hazelcast.instance.impl.HazelcastInstanceProxy;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.server.tcp.TcpServerConnection;
import com.hazelcast.internal.server.tcp.TcpServerConnectionManager;

public class LocalClient {

    private static final AtomicLong UUID_GEN = new AtomicLong();

    private final HazelcastInstanceImpl hzImpl;
    private final LocalChannel channel;
    private final TcpServerConnection connection;

    public LocalClient(HazelcastInstance hz, SocketAddress localAddress, SocketAddress remoteAddress, String username,
            String password) {
        this.hzImpl = ((HazelcastInstanceProxy) hz).getOriginal();
        this.channel = new LocalChannel(localAddress, remoteAddress);
        TcpServerConnectionManager cm = (TcpServerConnectionManager) hzImpl.node.server
                .getConnectionManager(EndpointQualifier.CLIENT);
        connection = new LocalConnection(cm, channel);
    }

    public AuthenticationStatus authenticate(String username, String password) {
        String clusterName = hzImpl.getConfig().getClusterName();
        byte serVersion = getSerializationService().getVersion();
        UUID uuid = new UUID(0, UUID_GEN.incrementAndGet());
        ClientMessage authnMsg = ClientAuthenticationCodec.encodeRequest(clusterName, username, password, uuid, "test",
                serVersion, "xx", "client", Collections.emptyList());
        ClientMessage authnResp = runClientTask(authnMsg, ClientAuthenticationCodec.RESPONSE_MESSAGE_TYPE);
        ResponseParameters respParams = ClientAuthenticationCodec.decodeResponse(authnResp);
        return AuthenticationStatus.getById(respParams.status);
    }

    public String mapPut(String name, String key, String value) {
        Data keyData = toData(key);
        ClientMessage putmsg = MapPutCodec.encodeRequest(name, keyData, toData(value), 0, 0);
        putmsg.setPartitionId(getPartitionId(keyData));
        ClientMessage resp = runClientTask(putmsg, MapPutCodec.RESPONSE_MESSAGE_TYPE);
        return toString(MapPutCodec.decodeResponse(resp));
    }

    public String mapGet(String name, String key) {
        Data keyData = toData(key);
        ClientMessage msg = MapGetCodec.encodeRequest(name, keyData, 0);
        msg.setPartitionId(getPartitionId(keyData));
        ClientMessage resp = runClientTask(msg, MapGetCodec.RESPONSE_MESSAGE_TYPE);
        return toString(MapGetCodec.decodeResponse(resp));
    }

    public String mapDelete(String name, String key) {
        Data keyData = toData(key);
        ClientMessage msg = MapRemoveCodec.encodeRequest(name, keyData, 0);
        msg.setPartitionId(getPartitionId(keyData));
        ClientMessage resp = runClientTask(msg, MapRemoveCodec.RESPONSE_MESSAGE_TYPE);
        return toString(MapRemoveCodec.decodeResponse(resp));
    }

    private Data toData(String str) {
        return getSerializationService().toData(str);
    }

    private String toString(Data data) {
        Object object = getSerializationService().toObject(data);
        if (object == null) {
            return null;
        }
        return object.toString();
    }

    private ClientMessage runClientTask(ClientMessage taskMessage, int expectedMessageType) {
        taskMessage.setConnection(connection);
        getClientEngine().accept(taskMessage);
        return channel.waitForResponse(expectedMessageType);
    }

    private InternalSerializationService getSerializationService() {
        return hzImpl.getSerializationService();
    }

    private ClientEngine getClientEngine() {
        return hzImpl.node.clientEngine;
    }

    private int getPartitionId(Data data) {
        return hzImpl.getPartitionService().getPartition(data).getPartitionId();
    }

}
