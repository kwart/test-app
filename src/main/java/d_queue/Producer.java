package d_queue;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

import utils.MobyNames;

public class Producer implements Runnable {

    private final BlockingQueue<Message> queue;

    public Producer(BlockingQueue<Message> queue) {
        this.queue = queue;
        
    }

    @Override
    public void run() {
        try {
            Random rnd = new Random();
            String producerName = "[" + System.currentTimeMillis() + "] ";
            while (true) {
                String msgString = producerName + MobyNames.getRandomName(rnd.nextInt());
                queue.put(new Message(msgString));
                SECONDS.sleep(rnd.nextInt(3)+1);
            }
        } catch (InterruptedException e) {
            // OK
        }
    }
}
