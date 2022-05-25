package cz.cacek.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.hazelcast.internal.networking.Channel;
import com.hazelcast.internal.nio.ConnectionLifecycleListener;
import com.hazelcast.internal.server.tcp.TcpServerConnection;
import com.hazelcast.internal.server.tcp.TcpServerConnectionManager;

public class LocalConnection extends TcpServerConnection {

    public LocalConnection(TcpServerConnectionManager cm, Channel channel) {
        super(cm, new SimpleLifecycleListener(), 0, channel, true);
    }

    @Override
    public InetAddress getInetAddress() {
        return ((InetSocketAddress) getChannel().remoteSocketAddress()).getAddress();
    }

    public static class SimpleLifecycleListener implements ConnectionLifecycleListener<TcpServerConnection> {
        @Override
        public void onConnectionClose(TcpServerConnection connection, Throwable t, boolean silent) {
            if (t != null) {
                t.printStackTrace();
            }
        }
    }
}
