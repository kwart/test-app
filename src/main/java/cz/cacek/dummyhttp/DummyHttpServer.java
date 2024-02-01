package cz.cacek.dummyhttp;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

/**
 * Simple HTTP (1.1) server which always succeeds with response code 200. The server is single threaded, it doesn't serve
 * requests in parallel.
 * <p>
 * Sample usage:
 *
 * <pre>
 * try (DummyHttpServer httpServer = new DummyHttpServer(8080, "Test".getBytes())) {
 *     new Thread(httpServer).start();
 *     httpServer.waitForStart();
 *     URL url = httpServer.getUrl();
 *     // use the server
 * }
 * </pre>
 */
public class DummyHttpServer implements Runnable, AutoCloseable {

    private static final byte[] STATUS_OK = "HTTP/1.1 200 OK\r\n\r\n".getBytes(UTF_8);
    private static final java.util.logging.Logger LOGGER = Logger.getLogger(DummyHttpServer.class.getName());

    private final byte[] content;
    private final int port;
    private volatile boolean shutdown = false;
    private final CountDownLatch startSignal = new CountDownLatch(1);

    /**
     * @param port port on which the server listens
     * @param content response body to be returned when a request comes
     */
    public DummyHttpServer(int port, byte[] content) {
        this.port = port;
        this.content = content;
    }

    public void run() {
        try (ServerSocket serverSocket = getServerSocketFactory().createServerSocket(port)) {
            serverSocket.setSoTimeout(500);
            LOGGER.fine("Server listening");
            startSignal.countDown();
            while (!shutdown) {
                try (Socket socket = serverSocket.accept(); OutputStream os = socket.getOutputStream(); 
                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8))) {
                    LOGGER.fine("Accepted " + socket.getRemoteSocketAddress());
                    br.readLine();
                    os.write(STATUS_OK);
                    os.write(content);
                } catch (SocketTimeoutException e) {
                    //ignore
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "", e);
                }
            }
        } catch (Exception ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Blocks until server socket is created.
     */
    public void waitForStart() {
        try {
            startSignal.await();
        } catch (InterruptedException e) {
            LOGGER.log(Level.INFO, "Interrupted", e);
        }
    }

    /**
     * Shuts down the server.
     */
    public void close() {
//        shutdown = true;
    }

    /**
     * Returns Protocol/Scheme used by this server.
     */
    public String getProtocol() {
        return "http";
    }

    public int getPort() {
        return port;
    }

    public URL getUrl() {
        try {
            return new URL(getProtocol(), "127.0.0.1", getPort(), "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected ServerSocketFactory getServerSocketFactory() throws IOException {
        return ServerSocketFactory.getDefault();
    }
}