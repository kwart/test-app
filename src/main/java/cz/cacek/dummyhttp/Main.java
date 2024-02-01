package cz.cacek.dummyhttp;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

public class Main {

    public static void main(String[] args) throws IOException {
        Set<String> tmpKeyStores = java.security.Security.getAlgorithms("KeyStore");
        TreeSet<String> result = new TreeSet<String>(tmpKeyStores);
        System.out.println(result);
    }

    public static void mainH(String[] args) throws IOException {
        System.setProperty("javax.net.debug", "ssl:handshake");

        try (DummyHttpServer httpServer = new DummyHttpsServer(8080, "Test".getBytes())) {
            new Thread(httpServer).start();
            httpServer.waitForStart();
            System.out.println("Server URL: " + httpServer.getUrl());
        }
    }

}
