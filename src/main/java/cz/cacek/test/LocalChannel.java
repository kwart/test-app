package cz.cacek.test;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.builtin.ErrorsCodec;
import com.hazelcast.client.impl.protocol.exception.ErrorHolder;
import com.hazelcast.internal.networking.ChannelOptions;
import com.hazelcast.internal.networking.InboundPipeline;
import com.hazelcast.internal.networking.OutboundFrame;
import com.hazelcast.internal.networking.OutboundPipeline;
import com.hazelcast.internal.networking.nio.AbstractChannel;

public class LocalChannel extends AbstractChannel {

    private final BlockingQueue<ClientMessage> messageQueue = new ArrayBlockingQueue<ClientMessage>(10);
    private final SocketAddress localAddress;
    private final SocketAddress remoteAddress;

    public LocalChannel(SocketAddress localAddress, SocketAddress remoteAddress) {
        super(null, false);
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    protected ClientMessage waitForResponse(int responseMessageType) {
        ClientMessage cm = null;
        try {
            do {
                cm = messageQueue.poll(30, TimeUnit.SECONDS);
                if (cm.getMessageType() == ErrorsCodec.EXCEPTION_MESSAGE_TYPE) {
                    List<ErrorHolder> err = ErrorsCodec.decode(cm);
                    // System.out.println(err.get(0).getClassName());
                    throw new ClientCallFailedException(err);
                }
            } while (cm.getMessageType() != responseMessageType);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return cm;
    }

    @Override
    public SocketAddress remoteSocketAddress() {
        return remoteAddress;
    }

    @Override
    public SocketAddress localSocketAddress() {
        return localAddress;
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
        return System.currentTimeMillis() - 10;
    }

    @Override
    public long lastWriteTimeMillis() {
        return System.currentTimeMillis() - 10;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean write(OutboundFrame frame) {
        // System.out.println(frame);
        ClientMessage cm = (ClientMessage) frame;
        try {
            messageQueue.offer(cm, 1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
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
