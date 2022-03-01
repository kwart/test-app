package cz.cacek.test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

public class TLSExecutorNonBlocking implements TLSExecutor {

    final ExecutorService executor;

    TLSExecutorNonBlocking() {
        this.executor = Executors.newFixedThreadPool(5);
    }

    public void executeHandshakeTasks(SSLEngine sslEngine) {
        List<Runnable> tasks = collectTasks(sslEngine);
        System.out.println("Executing tasks, size=" + tasks.size());
        for (Runnable task : tasks) {
            executor.execute(new HandshakeTask(task));
        }
    }

    public void shutdown() {
        executor.shutdown();
    }

    private static class HandshakeTask implements Runnable {
        private final Runnable task;

        HandshakeTask(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Task completed");
            }
        }
    }
}
