package d_queue;

import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {

    private final BlockingQueue<Message> queue;
    private final String name;

    public Consumer(BlockingQueue<Message> queue, String name) {
        this.queue = queue;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("[" + name + "] " + queue.take());
            }
        } catch (InterruptedException e) {
            // OK
        }
    }
}
