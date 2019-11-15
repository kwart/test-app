package cz.cacek.test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Random;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.security.SimpleTokenCredentials;

/**
 * Hazelcast Hello world!
 */
public class AppDelayed {

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

        Random rand = new Random();
        byte[] token = null;

        while (true) {
            System.out.println("Asking for a token.");
            try {
                GSSManager manager = GSSManager.getInstance();
                GSSContext gssContext = manager.createContext(manager.createName(spn, null), KRB5_OID, null,
                        GSSContext.DEFAULT_LIFETIME);
                gssContext.requestMutualAuth(false);
                gssContext.requestConf(false);
                gssContext.requestInteg(false);
                token = gssContext.initSecContext(new byte[0], 0, 0);
                if (!gssContext.isEstablished()) {
                    System.err.println("Unable to establish GSSContext by doing a single init step");
                    System.exit(1);
                }
            } catch (GSSException e) {
                e.printStackTrace();
                System.exit(3);
            }
            int nextSleepSec = rand.nextInt(15) + 30;
            System.out.println("Token received, sleeping for secs: " + nextSleepSec);
            try {
                Thread.sleep(1000L * nextSleepSec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ClientConfig clientConfig = new ClientConfig();
            clientConfig.getSecurityConfig().setCredentials(new SimpleTokenCredentials(token));
            clientConfig.getNetworkConfig().addAddress(hostname + ":" + System.getProperty("hazelcast.client.port", "10961"));
            System.out.println("Using the token");
            try {
                HazelcastClient.newHazelcastClient(clientConfig).shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static String nowStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK));
    }
}
