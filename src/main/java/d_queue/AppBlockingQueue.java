package d_queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

public class AppBlockingQueue {

    public static void main(String[] args) {

        BlockingQueue<Message> queue = getLocalQueue();
//        BlockingQueue<Message> queue = getHazelcastQueue();
//        BlockingQueue<Message> queue = getIgniteQueue();

        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(new Consumer(queue, "Consumer1"));
        threadPool.execute(new Consumer(queue, "Consumer2"));
        threadPool.execute(new Producer(queue));
    }


    private static BlockingQueue<Message> getLocalQueue() {
        return new LinkedBlockingQueue<>();
    }

    private static BlockingQueue<Message> getHazelcastQueue() {
        Config config = new Config().setLiteMember(true);
        return Hazelcast.newHazelcastInstance(config).getQueue("messages");
    }

    private static BlockingQueue<Message> getIgniteQueue() {
        IgniteConfiguration cfg = new IgniteConfiguration().setClientMode(true);
        return Ignition.start(cfg).queue("messages", 0, new CollectionConfiguration());
    }
}
