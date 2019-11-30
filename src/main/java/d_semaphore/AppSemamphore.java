package d_semaphore;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.ignite.IgniteSemaphore;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ISemaphore;

public class AppSemamphore {

    public static void main(String[] args) {
        final Semaphore semaphore = getLocalSemaphore(3);
        //Semaphore is not an interface 
//        final ISemaphore semaphore = getHazelcastSemaphore(3);
//        final IgniteSemaphore semaphore = getLocalSemaphore(3);

        Runnable task = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    semaphore.acquire();
                    System.out.println(LocalTime.now().getSecond() + " Semaphore acquired: " + Thread.currentThread().getName());
                    SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    // OK
                } finally {
                    semaphore.release();
                }
            }
        };
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        for (int i=0; i<5; i++) {
            threadPool.execute(task);
        }
    }

    private static Semaphore getLocalSemaphore(int permits) {
        return new Semaphore(permits, true);
    }

    private static ISemaphore getHazelcastSemaphore(int permits) {
        Config config = new Config().setLiteMember(true);
        ISemaphore semaphore = Hazelcast.newHazelcastInstance(config).getSemaphore("fooAndBar");
        semaphore.init(permits);
        return semaphore;
    }

    private static IgniteSemaphore getIgniteSemaphore(int permits) {
        IgniteConfiguration cfg = new IgniteConfiguration().setClientMode(true);
        return Ignition.start(cfg).semaphore("fooAndBar", permits, true, true);
    }
}
