package cz.cacek.test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The App!
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
        System.out.println("Threads " + nThreads);
        System.out.println("Print period " + printperiod);
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
            while (true) {
                try (Socket socket = new Socket()) {
                    socket.setReuseAddress(true);
                    socket.connect(serverAddresss);
                    long count = connectionCounter.incrementAndGet();
                    if (count % printperiod == 0L) {
                        System.out.println("Count of connections established: " + count);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
