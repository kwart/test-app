package cz.cacek.test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

/**
 * Starts a Hazelcast Client.
 */
public final class ClientServiceStarter implements Runnable {

    private static ClientServiceStarter INSTANCE;

    private volatile boolean shutdown = false;
    final HazelcastInstance hz = HazelcastClient.newHazelcastClient();

    private ClientServiceStarter() {
    }

    public synchronized static void start(String[] args) {
        if (INSTANCE == null) {
            INSTANCE = new ClientServiceStarter();
            new Thread(INSTANCE).start();
        }
    }

    public synchronized static void stop(String[] args) {
        if (INSTANCE != null) {
            INSTANCE.shutdown();
            INSTANCE = null;
        }
    }

    private void shutdown() {
        shutdown = true;
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                hz.getMap("test").put("timestamp", System.currentTimeMillis());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            INSTANCE = null;
            hz.shutdown();
        }
    }
}
