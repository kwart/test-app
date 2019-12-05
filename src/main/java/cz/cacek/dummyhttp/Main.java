package cz.cacek.dummyhttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class Main {

    public static void main(String[] args) throws IOException {
        System.setProperty("javax.net.debug", "ssl:handshake");

        try (DummyHttpsServer httpsServer = new DummyHttpsServer(8080, "Test".getBytes())) {
            HttpsURLConnection.setDefaultSSLSocketFactory(httpsServer.getClientSocketFactory());
            new Thread(httpsServer).start();
            httpsServer.waitForStart();
            URL url = httpsServer.getUrl();
            // use the server
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (null != (line = br.readLine())) {
                    System.out.println(line);
                }
            }
        }
    }

}
