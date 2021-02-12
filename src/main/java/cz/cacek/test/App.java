package cz.cacek.test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Hazelcast Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        Method m = NetworkInterface.class.getDeclaredMethod("getDefault");
        m.setAccessible(true);
        NetworkInterface ni = (NetworkInterface) m.invoke(null);
        System.out.println("Default interface: " + ni);

        MulticastSocket multicastSocket = new MulticastSocket(null);
        multicastSocket.setReuseAddress(true);
        multicastSocket.bind(new InetSocketAddress(54327));
        multicastSocket.setTimeToLive(32);
        if (args.length > 0) {
            multicastSocket.setLoopbackMode(true);
            multicastSocket.setInterface(findBindAddress());
        }
        multicastSocket.joinGroup(InetAddress.getByName("224.2.2.3"));
        System.out.println("Successfully joined");
    }

    private static InetAddress findBindAddress() throws IOException {
        System.out.println("Searching for bind address: ");
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        while (nis.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) nis.nextElement();
            System.out.println();
            System.out.println("nic name: " + ni.getName());
            System.out.println("nic display name: " + ni.getDisplayName());
            System.out.println("nic isLoopback(): " + ni.isLoopback());
            System.out.println("nic isPointToPoint(): " + ni.isPointToPoint());
            System.out.println("nic isVirtual(): " + ni.isVirtual());
            System.out.println("nic isUp(): " + ni.isUp());
            System.out.println("nic supportsMulticast(): " + ni.supportsMulticast());
            ArrayList<InetAddress> addrs = Collections.list(ni.getInetAddresses());
            System.out.println("nic addrs(): " + addrs);

            if (!ni.isLoopback() && !ni.isPointToPoint() && !ni.isVirtual() && ni.isUp() && ni.supportsMulticast()
                    && addrs.size() > 0) {
                System.out.println();
                System.out.println("adding nic: " + ni.getName());
                return addrs.get(0);
            }
        }
        return InetAddress.getByName("127.0.0.1");
    }
}
