package cz.cacek.dummyhttp;

import java.io.IOException;

public class Main {

    public static void mainhttp(String[] args) throws IOException {
        System.setProperty("javax.net.debug", "ssl:handshake");

        try (DummyHttpsServer httpsServer = new DummyHttpsServer(8080, "Test".getBytes())) {
            new Thread(httpsServer).start();
            httpsServer.waitForStart();
            System.out.println("Server URL:" + httpsServer.getUrl());
        }
    }

}
