package cz.cacek.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * The App!
 */
public class App {

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            usage("Two parameters expected!");
        }
        String host = args[0];
        int port = 5701;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            usage("Port has to be a number!");
        }
        try (Socket socket = new Socket(host, port)) {
            try (InputStream is = socket.getInputStream(); OutputStream os = socket.getOutputStream()) {
                os.write("HZC".getBytes(StandardCharsets.UTF_8));
                byte[] resp = new byte[3];
                for (int i = 0; i < resp.length; i++) {
                    resp[i] = (byte) is.read();
                }
                System.out.println(new String(resp, StandardCharsets.UTF_8));
            } catch (Exception e) {
                System.out.println("FAIL-IO");
            }
        } catch (Exception e) {
            System.out.println("FAIL-CONNECT");
        }
    }

    private static void usage(String msg) {
        System.err.println(msg);
        System.err.println();
        System.err.println("Usage:");
        System.err.println("\tjava -jar <appname.jar> <host> <port>");
        System.err.println();
        System.exit(1);
    }
}
