package cz.cacek.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.security.SimpleTokenCredentials;

/**
 * Hazelcast Hello world!
 */
public class App {

    private final static Oid KRB5_OID;
    static {
        try {
            KRB5_OID = new Oid("1.2.840.113554.1.2.2");
        } catch (GSSException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Usage:\n\tjava -jar app.jar [HazelcastMemberHostname] [KerberosRealm]");
            System.exit(1);
        }

        String hostname = args[0];
        String realm = args[1];
        System.setProperty("hazelcast.logging.type", "log4j2");
        String spn = "hazelcast/" + hostname + "@" + realm;

        try {
            GSSManager manager = GSSManager.getInstance();
            GSSContext gssContext = manager.createContext(manager.createName(spn, null), KRB5_OID, null,
                    GSSContext.DEFAULT_LIFETIME);
            gssContext.requestMutualAuth(false);
            gssContext.requestConf(false);
            gssContext.requestInteg(false);
            byte[] token = gssContext.initSecContext(new byte[0], 0, 0);
            if (!gssContext.isEstablished()) {
                System.err.println("Unable to establish GSSContext by doing a single init step");
                System.exit(1);
            }
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.getSecurityConfig().setCredentials(new SimpleTokenCredentials(token));
            clientConfig.getNetworkConfig().addAddress(hostname + ":" + System.getProperty("hazelcast.client.port", "10961"));
            final HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);
            final IMap<String, String> map = hz.getMap("data");
            Executors.newSingleThreadExecutor().submit(() -> quitCommandListener(hz, map));

            Random rand = new Random();
            while (true) {
                int nextSleepSec = rand.nextInt(60) + 1;
                map.put("date", nowStr());
                map.put("sleep", "" + nextSleepSec);
                try {
                    Thread.sleep(1000L * nextSleepSec);
                } catch (InterruptedException e) {
                    map.put("exception", e.getMessage());
                }
            }
        } catch (GSSException e) {
            e.printStackTrace();
            System.exit(3);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }

    private static void quitCommandListener(final HazelcastInstance hz, final IMap<String, String> map) {
        try (ServerSocket serverSocket = new ServerSocket(9797)) {
            while (true) {
                try (Socket socket = serverSocket.accept();
                        InputStreamReader isr = new InputStreamReader(socket.getInputStream())) {
                    char ch = (char) isr.read();
                    if ('q' == ch || 'Q' == ch) {
                        map.put("quit", nowStr());
                        hz.shutdown();
                        System.exit(0);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            map.put("quit", nowStr());
            map.put("exception", e.getMessage());
            hz.shutdown();
            System.exit(4);
        }
    }

    private static String nowStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK));
    }
}
