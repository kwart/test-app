package cz.cacek.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hazelcast Hello world!
 */
public class App {

    private static final int SOCKET_BUFFER_SIZE = 64 * 1024;
    private static final int SOCKET_TIMEOUT = 1000;

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread[] threads = new Thread[32];
        MulticastSocket multicastSocket = new MulticastSocket(null);
        multicastSocket.setReuseAddress(true);
        // bind to receive interface
        multicastSocket.bind(new InetSocketAddress(54327));
        multicastSocket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
        multicastSocket.setSendBufferSize(SOCKET_BUFFER_SIZE);
        multicastSocket.joinGroup(InetAddress.getByName("224.2.2." + (1 + new Random().nextInt(255))));
        multicastSocket.setSoTimeout(SOCKET_TIMEOUT);
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Sender(multicastSocket));
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        multicastSocket.close();
    }

    public static class Sender implements Runnable {

        private final DatagramSocket sock;

        public Sender(DatagramSocket sock) {
            this.sock = sock;
        }

        @Override
        public void run() {
            for (int i = 0; i < 16; i++) {
                try {
                    sock.send(new DatagramPacket(new byte[300], 0, InetAddress.getByName("224.2.2.3"), 54327));
                    // synchronized (sock) {
                    // }
                    // counter.incrementAndGet();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }
}
