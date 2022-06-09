package cz.cacek.test;

import java.io.IOException;
import java.security.AccessControlException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * The App!
 */
public class App {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("com.hazelcast");


    public App() {
    }

    public HazelcastInstance createClientInstance() throws IOException {
        return HazelcastClient.newHazelcastClient();
    }

    public void demo() throws IOException {
//        Hazelcast.newHazelcastInstance();
        HazelcastInstance client = createClientInstance();
        IMap<String, String> map = null;
        while (map == null) {
            try {
                map = client.getMap("timestamps");
            } catch (AccessControlException ae) {
                System.out.println("Unable to work with timestamps map: " + ae);
                sleep();
            }
        }

        try {
            while (true) {
                System.out.print("Reading timestamp: ");
                try {
                    System.out.println(map.get("timestamp"));
                } catch (AccessControlException ae) {
                    System.out.println(ae.getMessage());
                }
                System.out.print("Setting new timestamp: ");
                try {
                    LocalDateTime localDateTime = LocalDateTime.now();
                    map.put("timestamp", localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
                    System.out.println("passed");
                } catch (AccessControlException ae) {
                    System.out.println(ae.getMessage());
                }
                sleep();
            }
        } finally {
            client.shutdown();
        }
    }

    protected void sleep() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        new App().demo();
    }

    static {
        java.util.logging.ConsoleHandler ch = new java.util.logging.ConsoleHandler();
        ch.setLevel(java.util.logging.Level.WARNING);
        LOGGER.addHandler(ch);
        LOGGER.setLevel(java.util.logging.Level.WARNING);
    }

}
