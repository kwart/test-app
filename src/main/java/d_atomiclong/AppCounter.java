package d_atomiclong;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IAtomicLong;

public class AppCounter {

    public static void main(String[] args) throws Exception {
         AtomicLong counter = getLocalCounter();
        // IAtomicLong counter = getHazelcastCounter();
        // IgniteAtomicLong counter = getIgniteCounter();

         // Infinispan has its own asynchronous counter API, which doesn't extend java.util.concurrent.locks.Lock. 

        try (ServerSocket server = new ServerSocket()) {
            server.bind(null);
            System.out.println("Listening on port: " + server.getLocalPort());
            while (true) {
                try (Socket socket = server.accept()) {
                    long reqNr = counter.incrementAndGet();
                    socket.getOutputStream().write((reqNr + "\n").getBytes(US_ASCII));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static AtomicLong getLocalCounter() {
        return new AtomicLong(0L);
    }

    private static IAtomicLong getHazelcastCounter() {
        Config config = new Config().setLiteMember(true);
        return Hazelcast.newHazelcastInstance(config).getAtomicLong("counter");
    }

    private static IgniteAtomicLong getIgniteCounter() {
        IgniteConfiguration cfg = new IgniteConfiguration().setClientMode(true);
        return Ignition.start(cfg).atomicLong("counter", 0, true);
    }
}
