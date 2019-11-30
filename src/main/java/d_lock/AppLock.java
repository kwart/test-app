package d_lock;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

public class AppLock {

    public static void main(String[] args) {
        final Lock lock = getLocalLock();
        // final Lock lock = getHazelcastLock();
        // final Lock lock = getIgniteLock();

        // Infinispan has its own ClusteredLock API, which doesn't extend java.util.concurrent.locks.Lock. 

        Runnable task = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                lock.lock();
                try {
                    System.out.println(LocalTime.now().getSecond() + " Lock has " + Thread.currentThread().getName());
                    SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // OK
                } finally {
                    lock.unlock();
                }
            }
        };
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(task);
        threadPool.execute(task);

    }


    private static Lock getLocalLock() {
        return new ReentrantLock(true);
    }

    private static Lock getHazelcastLock() {
        Config config = new Config().setLiteMember(true);
        return Hazelcast.newHazelcastInstance(config).getLock("lock");
    }

    private static Lock getIgniteLock() {
        IgniteConfiguration cfg = new IgniteConfiguration().setClientMode(true);
        return Ignition.start(cfg).reentrantLock("lock", true, true, true);
    }
}
