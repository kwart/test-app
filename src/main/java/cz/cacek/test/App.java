package cz.cacek.test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class App implements Callable<Boolean>{

    private static final int DATAGRAM_BUFFER_SIZE = 64 * 1024;

    final InetAddress bindAddress;
    final boolean isLoopbackMode;
    final boolean isSetInterface;
    final boolean isSetBindAddress;

    public App(InetAddress bindAddress, boolean isLoopbackMode, boolean isSetInterface, boolean isSetBindAddress) {
        this.bindAddress = bindAddress;
        this.isLoopbackMode = isLoopbackMode;
        this.isSetInterface = isSetInterface;
        this.isSetBindAddress = isSetBindAddress;
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length != 4) {
            System.err.println("Unexpected number of arguments");
            System.err.println("Usage:");
            System.err.println("\tjava -jar app.jar [bindAddress] [loopbackModeEnabled] [setInterface] [setBindAddress]");
            System.exit(2);
        }
        InetAddress bindAddress = InetAddress.getByName(args[0]);
        boolean isLoopbackMode = Boolean.parseBoolean(args[1]);
        boolean isSetInterface = Boolean.parseBoolean(args[2]);
        boolean isSetBindAddress = Boolean.parseBoolean(args[3]);
        System.out.println(new App(bindAddress, isLoopbackMode, isSetInterface, isSetBindAddress).call());
    }

    public Boolean call() {
        String uuid = UUID.randomUUID().toString();
        boolean otherFound = false;
        try (MulticastSocket multicastSocket = new MulticastSocket(null)) {
            multicastSocket.setReuseAddress(true);
            int port = 54327;
            multicastSocket.bind(isSetBindAddress ? new InetSocketAddress(bindAddress, port) : new InetSocketAddress(port));

            multicastSocket.setLoopbackMode(!isLoopbackMode);
            if (isSetInterface) {
                multicastSocket.setInterface(bindAddress);
            }
            InetAddress groupAddress = InetAddress.getByName("224.2.2.3");
            multicastSocket.joinGroup(groupAddress);

            byte[] packetData = uuid.getBytes();
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length, groupAddress, port);
            Thread sender = new Thread(() -> {
                try {
                    while (true) {
                        multicastSocket.send(packet);
                        TimeUnit.SECONDS.sleep(2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            sender.setDaemon(true);
            sender.start();

            DatagramPacket datagramPacketReceive = new DatagramPacket(new byte[DATAGRAM_BUFFER_SIZE], DATAGRAM_BUFFER_SIZE);
            multicastSocket.setSoTimeout(2000);
            LocalDateTime end = LocalDateTime.now().plusSeconds(8);
            while (LocalDateTime.now().isBefore(end)) {
                try {
                    multicastSocket.receive(datagramPacketReceive);
                    String receivedStr = new String(datagramPacketReceive.getData(), datagramPacketReceive.getOffset(),
                            datagramPacketReceive.getLength());
//                    System.out.println("Received: '" + receivedStr + "' from " + datagramPacketReceive.getSocketAddress() + " ("
//                            + (uuid.equals(receivedStr) ? "self" : "other") + ")");
                    otherFound = otherFound || !uuid.equals(receivedStr);
                } catch (SocketTimeoutException st) {
                }
            }
            multicastSocket.leaveGroup(groupAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return otherFound;
    }
}
