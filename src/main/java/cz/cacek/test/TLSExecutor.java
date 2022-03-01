package cz.cacek.test;

import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLEngine;

interface TLSExecutor {

    void executeHandshakeTasks(SSLEngine sslEngine);

    default void shutdown() {
    };

    default List<Runnable> collectTasks(SSLEngine sslEngine) {
        List<Runnable> tasks = new LinkedList<Runnable>();

        Runnable task;
        while ((task = sslEngine.getDelegatedTask()) != null) {
            tasks.add(task);
        }
        return tasks;
    }
}
