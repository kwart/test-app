package d_concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;

public class AppBlockingQueue {

    public static final String[] DOCKER_IP_ADDRESSES = IntStream.range(2, 10).mapToObj(i -> "172.17.0." + i)
            .toArray(i -> new String[i]);

    public static void main(String[] args) {
        BlockingQueue<Message> queue = getLocalQueue();
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(new Consumer(queue));
    }


    private static BlockingQueue<Message> getLocalQueue() {
        return new LinkedBlockingQueue<>();
    }

    private static BlockingQueue<Message> getHazelcastQueue() {
        ClientConfig clientConfig = new ClientConfig();
        for (int i = 2; i < 10; i++) {
            clientConfig.getNetworkConfig().addAddress(DOCKER_IP_ADDRESSES);
        }
        return HazelcastClient.newHazelcastClient(clientConfig).getQueue("messages");
    }

    private static BlockingQueue<Message> getIgniteQueue() {
        IgniteConfiguration cfg = new IgniteConfiguration().setClientMode(true);
        return Ignition.start(cfg).queue("messages", 0, new CollectionConfiguration());
    }
}
