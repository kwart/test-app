package org.jboss.test;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import junit.framework.Assert;

/**
 * Tests {@link HashMap} thread (un)safety.
 */
public class HashMapTest {

    /**
     * Count of entries in the map after which we'll stop adding new ones.
     */
    private static final int MIN_ENTRIES = 10_000_000;

    /**
     * Test filling a HashMap using single worker thread. This one is expected to pass. There are a happens-before relations
     * between controlling thread and the single thread (when calling Thread.start() and Thread.join()).
     */
    @Test
    public void testFillWithSingleThread() throws InterruptedException {
        assertMapSafeInThreadPool(1);
    }

    /**
     * Test filling a HashMap using 2 worker threads. This test will probably fail.
     */
    @Test
    public void testFillWithTwoThreads() throws InterruptedException {
        assertMapSafeInThreadPool(2);
    }

    /**
     * Test filling a HashMap using 3 worker threads. This test will probably fail.
     */
    @Test
    public void testFillWithThreeThreads() throws InterruptedException {
        assertMapSafeInThreadPool(3);
    }

    /**
     * Treeifying bins (where entries with the same key.hashCode goes) is done after reaching a threshold
     * ({@code HashMap#TREEIFY_THRESHOLD}). Let's try to hunt the problem which can occur there (ClassCastException).
     */
    @Test
    public void huntForTreeifyingIssues() throws InterruptedException {
        System.out.println("Starting hunt for treeifying issues. It should run a minute at most.");
        final HashMap<ObjectWithFixedHashcode, Void> map = new HashMap<>();
        final AddWithSameHashCodeThread addingThread1 = new AddWithSameHashCodeThread(map, "T1");
        final AddWithSameHashCodeThread addingThread2 = new AddWithSameHashCodeThread(map, "T2");

        addingThread1.start();
        addingThread2.start();

        // active waiting for an ClassCastException
        while (addingThread1.isAlive() && addingThread2.isAlive() && !addingThread1.hitClassCastException
                && !addingThread2.hitClassCastException) {
            // wait a sec
            Thread.sleep(1000L);
        }
        addingThread1.interrupt();
        addingThread2.interrupt();
        Assert.assertFalse("Caught ClassCastException when putting entry to the HashMap",
                addingThread1.hitClassCastException || addingThread2.hitClassCastException);
    }

    /**
     * Implementation of {@link HashMap} thread safety test with given count of threads.
     * 
     * @param threadCount
     */
    private void assertMapSafeInThreadPool(int threadCount) throws InterruptedException {
        System.out.println("Starting test with filling map using " + threadCount + " thread(s)");
        final HashMap<Integer, String> map = new HashMap<>();
        final AtomicInteger counter = new AtomicInteger();

        final Thread[] threads = new Thread[threadCount];
        // create workers
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new FillMapRunnable(map, counter), "T" + i);
            threads[i].start();
        }
        // wait for all the workers to finish
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }
        // check if the counter reached the expected size. (This's not expected to fail.)
        final int expectedMapSize = counter.get();
        Assert.assertFalse("Creating entries finished too early", expectedMapSize < MIN_ENTRIES);

        // check if we lost some data in between
        int missingCount = 0;
        for (int i = 1; i <= expectedMapSize; i++) {
            if (null == map.get(i)) {
                missingCount++;
            }
        }
        Assert.assertEquals("Unexpected count of null-valued entries found", 0, missingCount);
    }

    /**
     * Runnable which adds new entries to given {@link HashMap}. Newly added keys are taken from given (shared) counter and as a
     * value is used the current thread name.
     */
    public static class FillMapRunnable implements Runnable {

        private final HashMap<Integer, String> map;
        private final AtomicInteger counter;

        public FillMapRunnable(HashMap<Integer, String> map, AtomicInteger counter) {
            this.map = Objects.requireNonNull(map);
            this.counter = Objects.requireNonNull(counter);
        }

        @Override
        public void run() {
            final String val = Thread.currentThread().getName();
            int internalCounter = 0;
            int globalCounter;
            while (counter.get() < MIN_ENTRIES) {
                map.put((globalCounter = counter.incrementAndGet()), val);
                internalCounter++;
                int n = map.size();
                // the internal counter should never be greater than the map size;
                if (n < internalCounter) {
                    new AssertionError("Unexpected map size - Internal maximum estimation = " + internalCounter
                            + ", Reported Map size = " + n + ", Global counter = " + globalCounter);
                } else {
                    // update internal counter to last map size value
                    internalCounter = n;
                }
            }
        }
    }

    /**
     * Runnable which adds entries with the same hashcode. The thread runs 1 minute at most.
     */
    public static class AddWithSameHashCodeThread extends Thread {

        private final HashMap<ObjectWithFixedHashcode, Void> map;

        volatile boolean hitClassCastException;

        public AddWithSameHashCodeThread(HashMap<ObjectWithFixedHashcode, Void> map, String name) {
            super(name);
            this.map = Objects.requireNonNull(map);
        }

        @Override
        public void run() {
            final long endTime = System.currentTimeMillis() + 60 * 1000;
            try {
                while (!interrupted() && System.currentTimeMillis() < endTime) {
                    map.put(new ObjectWithFixedHashcode(), null);
                    if (map.size() > 100)
                        map.clear();
                }
            } catch (ClassCastException e) {
                hitClassCastException = true;
                e.printStackTrace();
            }
        }
    }
}
