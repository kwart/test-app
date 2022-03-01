package cz.cacek.test;

import java.util.List;

import javax.net.ssl.SSLEngine;

public class TLSExecutorBlocking implements TLSExecutor {

    public void executeHandshakeTasks(SSLEngine sslEngine) {
        List<Runnable> tasks = collectTasks(sslEngine);
        System.out.println("Executing tasks, size=" + tasks.size());
        for (Runnable task : tasks) {
            task.run();
            System.out.println("Task completed");
        }
    }
}
