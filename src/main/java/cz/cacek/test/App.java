package cz.cacek.test;

import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class App {

    private static final int DATAGRAM_BUFFER_SIZE = 64 * 1024;
    private static int PORT = 54327;
    private static InetAddress GROUP_ADDRESS;
    static {
        try {
            GROUP_ADDRESS = InetAddress.getByName("224.2.2.3");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    final InetAddress bindAddress;
    final boolean isLoopbackMode;
    final boolean isSetInterface;
    final boolean isSetBindAddress;

    final String uuid;

    private volatile boolean received;

    public App(InetAddress bindAddress, boolean isLoopbackMode, boolean isSetInterface, boolean isSetBindAddress) {
        this.bindAddress = bindAddress;
        this.isLoopbackMode = isLoopbackMode;
        this.isSetInterface = isSetInterface;
        this.isSetBindAddress = isSetBindAddress;
        this.uuid = UUID.randomUUID().toString();
    }

    public static void main(String[] args) throws Exception {
        // if (args == null || args.length != 4) {
        // System.err.println("Unexpected number of arguments");
        // System.err.println("Usage:");
        // System.err.println("\tjava -jar app.jar [bindAddress] [loopbackModeEnabled] [setInterface] [setBindAddress]");
        // System.exit(2);
        // }
        // InetAddress bindAddress = InetAddress.getByName(args[0]);
        // boolean isLoopbackMode = Boolean.parseBoolean(args[1]);
        // boolean isSetInterface = Boolean.parseBoolean(args[2]);
        // boolean isSetBindAddress = Boolean.parseBoolean(args[3]);
        // App app = new App(bindAddress, isLoopbackMode, isSetInterface, isSetBindAddress);
        for(NetworkInterface ni: Collections.list(NetworkInterface.getNetworkInterfaces())) {
            boolean skip = ni.isVirtual() || !ni.isUp();
            if (skip) continue;
            for (InetAddress ia: Collections.list(ni.getInetAddresses())) {
                if (ia instanceof Inet4Address) {
                    for (boolean loopbackMode : Arrays.asList(false, true)) {
                        for (boolean setInterface : Arrays.asList(false, true)) {
                            for (boolean setBindAddr : Arrays.asList(false, true)) {
                                App app = new App(ia, loopbackMode, setInterface, setBindAddr);
                                app.sender();
                                System.out.println(app.received() 
                                        + "\t" + ia.getHostAddress()
                                        + "\t" + loopbackMode
                                        + "\t" + setInterface
                                        + "\t" + setBindAddr
                                        );
                            }
                        }
                    }
                }
            }
        }
    }

    private MulticastSocket configureSocket() throws Exception {
        MulticastSocket multicastSocket = new MulticastSocket(null);
        multicastSocket.setReuseAddress(true);
        multicastSocket.bind(isSetBindAddress ? new InetSocketAddress(bindAddress, PORT) : new InetSocketAddress(PORT));

        multicastSocket.setLoopbackMode(!isLoopbackMode);
        if (isSetInterface) {
            multicastSocket.setInterface(bindAddress);
        }
        multicastSocket.joinGroup(GROUP_ADDRESS);
        return multicastSocket;
    }

    public void sender() {
        Thread sender = new Thread(() -> {
            try (MulticastSocket multicastSocket = configureSocket()) {
                byte[] packetData = uuid.getBytes();
                DatagramPacket packet = new DatagramPacket(packetData, packetData.length, GROUP_ADDRESS, PORT);
                while (!received) {
//                    System.out.println("Sending");
                    multicastSocket.send(packet);
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
        });
        sender.setDaemon(true);
        sender.start();
    }

    public boolean received() {
        try (MulticastSocket multicastSocket = configureSocket()) {
            DatagramPacket datagramPacketReceive = new DatagramPacket(new byte[DATAGRAM_BUFFER_SIZE], DATAGRAM_BUFFER_SIZE);
            multicastSocket.setSoTimeout(1000);
            LocalDateTime end = LocalDateTime.now().plusSeconds(5);
            while (LocalDateTime.now().isBefore(end)) {
                try {
                    multicastSocket.receive(datagramPacketReceive);
                    String receivedStr = new String(datagramPacketReceive.getData(), datagramPacketReceive.getOffset(),
                            datagramPacketReceive.getLength());
//                    System.out.println("Received: '" + receivedStr + "' from " + datagramPacketReceive.getSocketAddress() + "("
//                            + (uuid.equals(receivedStr) ? "self" : "other") + ")");
                    received = received || uuid.equals(receivedStr);
                    if (received) {
                        return true;
                    }
                } catch (SocketTimeoutException st) {
                }
            }
            multicastSocket.leaveGroup(GROUP_ADDRESS);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return false;
    }
}
