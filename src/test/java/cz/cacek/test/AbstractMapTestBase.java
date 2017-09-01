package cz.cacek.test;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import junit.framework.Assert;

/**
 * Abstract parent for smoke-testing thread safety of a {@link Map} implementation. Child classes implements the
 * {@link #createMapInstance()} which returns tested instance.
 */
public abstract class AbstractMapTestBase {

    /**
     * Count of entries in the map after which we'll stop adding new ones. If you don't hit the failures with this setting in
     * your environment, try to increase (x 10 for instance).
     */
    private static final int TEST_ENTRIES_COUNT = 1_000_000;

    /**
     * Placeholder map entry value. It's necessary for {@link Map} implementations which don't allow {@code null} values.
     */
    private static final Object NULL_OBJECT = new Object();

    abstract protected <K, V> Map<K, V> createMapInstance();

    /**
     * Test filling a Map using single worker thread. This one is expected to pass. There are a happens-before relations between
     * controlling thread and the single thread (when calling Thread.start() and Thread.join()).
     */
    @Test
    public void testFillWithSingleThread() throws InterruptedException {
        assertMapFillingVisibleForGivenThreadCount(1);
    }

    /**
     * Test filling a Map using 2 worker threads. This test will probably fail.
     */
    @Test
    public void testFillWithTwoThreads() throws InterruptedException {
        assertMapFillingVisibleForGivenThreadCount(2);
    }

    /**
     * Test filling a Map using 3 worker threads. This test will probably fail.
     */
    @Test
    public void testFillWithThreeThreads() throws InterruptedException {
        assertMapFillingVisibleForGivenThreadCount(3);
    }

    /**
     * Treeifying bins (where entries with the same key.hashCode goes) is done after reaching a threshold
     * ({@code HashMap#TREEIFY_THRESHOLD}). Let's try to hunt the problem which can occur there (a {@link ClassCastException}).
     */
    @Test
    public void huntForTreeifyingIssues() throws InterruptedException {
        System.out.println("Starting hunt for treeifying issues. It should run a minute at most.");
        final Map<ObjectWithFixedHashcode, Object> map = createMapInstance();
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
        Assert.assertFalse("Caught ClassCastException when putting entry to the Map",
                addingThread1.hitClassCastException || addingThread2.hitClassCastException);
    }

    /**
     * Test removing entries from a Map using 2 worker threads. The Map instance is filled with values first and then 2 worker
     * threads are used to remove entries from the map. After the worker threads successfully finishes, the size of the map is
     * verified (0 is expected).
     */
    @Test
    public void testRemoveWithTwoThreads() throws InterruptedException {
        System.out.println("Starting test which removes from the map  using 2 threads");

        // Create a Map instance and fill it with the predefined count of entries. Key is the same as the value and both are
        // just unique integers.
        final Map<Integer, Integer> map = createMapInstance();
        for (int i = 0; i < TEST_ENTRIES_COUNT; i++) {
            map.put(i, i);
        }
        // Let's use atomic counter for removing, so we are sure the removed entry (with given key retrieved from counter) is
        // unique across the threads
        final AtomicInteger counter = new AtomicInteger(TEST_ENTRIES_COUNT);
        final Thread[] threads = new Thread[2];
        // create workers
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new RemoveFromMapRunnable(map, counter), "T" + i);
            // starting the thread creates happens-before ordering between this control thread and the worker. I.e. The worker's
            // run() method is guaranteed to see what happened in this (executing) thread before calling thread.start().
            threads[i].start();
        }

        // The worker threads are removing from the map now.
        // And it depends on how the map.remove() method is implemented, if the changes done by remove() call in one thread are
        // guaranteed to be visible in the other worker thread because we don't provide additional synchronization ourselves in
        // the
        // workers.
        // It means, if the visibility guarantee (between threads) is missing in the remove() implementation, the values in
        // worker
        // threads could be stored just in local memory. I.e.:
        // - not guaranteed to be invalidated in local memory before write;
        // - not guaranteed to be flushed to main memory after write;

        // wait for all the workers to finish
        for (int i = 0; i < threads.length; i++) {
            // again a happens-before is created between the worker thread and this control thread;
            // If the map.remove() changes would be guaranteed to be visible across the threads, then we're creating here (by
            // calling the
            // Thread.join()) a guarantee to see the correct state in this control thread.
            threads[i].join();
        }
        // Check the counter value. (This's not expected to fail.)
        Assert.assertTrue("Unexpected counter size after finishing", counter.get() < 1);
        // If the remove() operation would be thread-safe, we would see empty map
        // here.
        Assert.assertEquals("Unexpected Map size after finishing", 0, map.size());
    }

    /**
     * Runs the filling {@link Map} test with given count of worker threads. Checks if the final size of the map is the expected
     * one.
     * 
     * @param threadCount count of worker threads to be created and started to fill the map
     */
    private void assertMapFillingVisibleForGivenThreadCount(int threadCount) throws InterruptedException {
        System.out.println("Starting test with filling map using " + threadCount + " thread(s)");

        // Create an empty map instance.
        final Map<Integer, String> map = createMapInstance();

        // Let's use atomic counter, so the filled value are unique across the threads. I.e. every value is filled exactly once
        // (in just one thread).
        final AtomicInteger counter = new AtomicInteger();

        final Thread[] threads = new Thread[threadCount];
        // create workers
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new FillMapRunnable(map, counter), "T" + i);
            // starting the thread creates happens-before ordering between this control thread and the worker. I.e. The worker's
            // run() method is guaranteed to see what happened in this (executing) thread before calling thread.start().
            threads[i].start();
        }

        // The worker threads are filling the map up to the TEST_ENTRIES_COUNT now.
        // And it depends on how the map.put() method is implemented, if the change done by put() call in one thread is
        // guaranteed to be visible in other worker threads because we don't provide additional synchronization ourselves in the
        // workers.
        // It means, if the visibility guarantee (between threads) is missing in the put() implementation, the values in worker
        // threads could be stored just in local memory. I.e.:
        // - not guaranteed to be invalidated in local memory before write;
        // - not guaranteed to be flushed to main memory after write;

        // Let's wait for all the workers to finish.
        for (int i = 0; i < threadCount; i++) {
            // again a happens-before is created between the worker thread and this control thread;
            // If the map.put() changes would be guaranteed to be visible across the threads, we create by calling the
            // Thread.join() a guarantee here to see the correct state in this control thread.
            threads[i].join();
        }

        // Check if the counter reached the expected size. (This's not expected to fail.)
        final int expectedMapSize = counter.get();
        Assert.assertFalse("Creating entries finished too early", expectedMapSize < TEST_ENTRIES_COUNT);

        // Check if we lost some data in between.
        // If there was only one worker thread, we are safe. We have 2 happens-before (start() and join()) which guarantees we
        // would see correct content of the map here,
        // because we didn't touch the map between calling start() and join() in this controlling thread.
        int missingCount = 0;
        for (int i = 1; i <= expectedMapSize; i++) {
            if (null == map.get(i)) {
                missingCount++;
            }
        }
        Assert.assertEquals("Unexpected count of null-valued entries found", 0, missingCount);
    }

    /**
     * Runnable which adds new entries to given {@link Map}. Newly added keys are taken from given (shared) counter and as a
     * value is used the current thread name.
     */
    public static class FillMapRunnable implements Runnable {

        private final Map<Integer, String> map;
        private final AtomicInteger counter;

        public FillMapRunnable(Map<Integer, String> map, AtomicInteger counter) {
            this.map = Objects.requireNonNull(map);
            this.counter = Objects.requireNonNull(counter);
        }

        @Override
        public void run() {
            final String val = Thread.currentThread().getName();
            int internalCounter = 0;
            int globalCounter;
            while (counter.get() < TEST_ENTRIES_COUNT) {
                map.put((globalCounter = counter.incrementAndGet()), val);
                internalCounter++;
                int n = map.size();
                // The internal counter should never be greater than the map
                // size. But when the visibility guarantee is missing in map.put() and/or map.size() implementation for some
                // involved map member field, we could hit this "size didn't increased" problem.
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
     * Runnable which removes entries from given {@link Map}. Newly
     */
    public static class RemoveFromMapRunnable implements Runnable {

        private final Map<Integer, Integer> map;
        private final AtomicInteger counter;

        public RemoveFromMapRunnable(Map<Integer, Integer> map, AtomicInteger counter) {
            this.map = Objects.requireNonNull(map);
            this.counter = Objects.requireNonNull(counter);
        }

        @Override
        public void run() {
            int removeKey;
            while ((removeKey = counter.decrementAndGet()) >= 0) {
                // Remove the entry with key retrieved from the atomic counter and check that the removed entry has expected
                // value. This assert should pass, because filling
                // the map (in control thread) is visible for this thread as the Thread.start() created the happens-before
                // ordering.
                Assert.assertEquals(map.remove(removeKey), new Integer(removeKey));
            }
        }
    }

    /**
     * Thread which adds entries with the same hashcode. The thread runs 1 minute at most.
     */
    public static class AddWithSameHashCodeThread extends Thread {

        private final Map<ObjectWithFixedHashcode, Object> map;

        volatile boolean hitClassCastException;

        public AddWithSameHashCodeThread(Map<ObjectWithFixedHashcode, Object> map, String name) {
            super(name);
            this.map = Objects.requireNonNull(map);
        }

        @Override
        public void run() {
            final long endTime = System.currentTimeMillis() + 60 * 1000;
            try {
                while (!interrupted() && System.currentTimeMillis() < endTime) {
                    map.put(new ObjectWithFixedHashcode(), NULL_OBJECT);
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
