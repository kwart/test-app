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
    final Boolean loopbackModeEnabled;
    final boolean isSetInterface;

    final String uuid;

    private volatile boolean received;

    public App(InetAddress bindAddress, Boolean isLoopbackMode, boolean isSetInterface) {
        this.bindAddress = bindAddress;
        this.loopbackModeEnabled = isLoopbackMode;
        this.isSetInterface = isSetInterface;
        this.uuid = UUID.randomUUID().toString();
    }

    public static void main(String[] args) throws Exception {
        for(NetworkInterface ni: Collections.list(NetworkInterface.getNetworkInterfaces())) {
            boolean skip = ni.isVirtual() || !ni.isUp();
            if (skip) continue;
            for (InetAddress ia: Collections.list(ni.getInetAddresses())) {
                if (ia instanceof Inet4Address) {
                    for (Boolean loopbackMode : Arrays.asList(null, false, true)) {
                        for (boolean setInterface : Arrays.asList(false, true)) {
                            App app = new App(ia, loopbackMode, setInterface);
                            app.sender();
                            System.out.println(app.received() 
                                    + "\t" + ia.getHostAddress()
                                    + "\t" + loopbackMode
                                    + "\t" + setInterface
                                    );
                        }
                    }
                }
            }
        }
    }

    private MulticastSocket configureSocket() throws Exception {
        MulticastSocket multicastSocket = new MulticastSocket(null);
        multicastSocket.setReuseAddress(true);
        multicastSocket.bind(new InetSocketAddress(PORT));

        if (loopbackModeEnabled != null) {
            multicastSocket.setLoopbackMode(!loopbackModeEnabled);
        }
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
                e.printStackTrace();
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
            System.err.println("Receiver error in " + this);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return "App [bindAddress=" + bindAddress + ", loopbackModeEnabled=" + loopbackModeEnabled + ", isSetInterface="
                + isSetInterface + "]";
    }

    
}
