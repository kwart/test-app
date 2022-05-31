package cz.cacek.test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <pre>
 * Before running the app, configure the TCP:
 * 
 * sudo su -
 * echo 1 > /proc/sys/net/ipv4/tcp_tw_reuse
 * echo 1024 65535 > /proc/sys/net/ipv4/ip_local_port_range
 * </pre>
 */
public class App {

    final InetSocketAddress serverAddresss;
    final AtomicLong connectionCounter = new AtomicLong(0);
    final int printperiod = Integer.getInteger("client.printperiod", 100000);

    public App(InetSocketAddress serverAddress) {
        this.serverAddresss = serverAddress;
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length != 2) {
            printUsage();
            System.exit(1);
        }
        String host = args[0];
        int port = 0;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            printUsage();
            System.exit(1);
        }
        InetSocketAddress endpoint = new InetSocketAddress(host, port);
        new App(endpoint).run();
    }

    public void run() {
        int nThreads = Integer.getInteger("client.threads", Runtime.getRuntime().availableProcessors() * 4);
        System.out.println("Starting clients for server " + serverAddresss);
        System.out.println("Threads: -Dclient.threads=" + nThreads);
        System.out.println("Print period: -Dclient.printperiod=" + printperiod);
        System.out.println();
        Thread[] threads = new Thread[nThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new ConnectionEstablisher());
            threads[i].start();
        }
    }

    static void printUsage() {
        System.err.println("Usage:");
        System.err.println("\tjava -jar test-app.jar [address] [port]");
        System.err.println();
    }

    class ConnectionEstablisher implements Runnable {

        @Override
        public void run() {
            long count = connectionCounter.incrementAndGet();
            while (count < Integer.MAX_VALUE) {
                try (Socket socket = new Socket()) {
                    socket.setReuseAddress(true);
                    count = connectionCounter.incrementAndGet();
                    socket.connect(serverAddresss);
                    if (count % printperiod == 0L) {
                        System.out.println(LocalDateTime.now() + "Count of connections established: " + count);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    System.err.println("Counter: " + count);
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e1) {
//                        e1.printStackTrace();
//                    }
//                    System.exit(3);
                }
            }
        }

    }

}
