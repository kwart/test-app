package cz.cacek.test;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import junit.framework.Assert;

/**
 * Abstract parent for smoke-testing thread safety of a {@link Map}
 * implementation. Child classes implements the {@link #createMapInstance()}
 * which returns tested instance.
 */
public abstract class AbstractMapTestBase {

	/**
	 * Count of entries in the map after which we'll stop adding new ones. If
	 * you don't hit the failures with this setting in your environment, try to
	 * increase (x 10 for instance).
	 */
	private static final int TEST_ENTRIES_COUNT = 1_000_000;

	/**
	 * Placeholder map entry value. It's necessary for {@link Map}
	 * implementations which don't allow {@code null} values.
	 */
	private static final Object NULL_OBJECT = new Object();

	abstract protected <K, V> Map<K, V> createMapInstance();

	/**
	 * Test filling a Map using single worker thread. This one is expected to
	 * pass. There are a happens-before relations between controlling thread and
	 * the single thread (when calling Thread.start() and Thread.join()).
	 */
	@Test
	public void testFillWithSingleThread() throws InterruptedException {
		assertMapSafeInThreadPool(1);
	}

	/**
	 * Test filling a Map using 2 worker threads. This test will probably fail.
	 */
	@Test
	public void testFillWithTwoThreads() throws InterruptedException {
		assertMapSafeInThreadPool(2);
	}

	/**
	 * Test filling a Map using 3 worker threads. This test will probably fail.
	 */
	@Test
	public void testFillWithThreeThreads() throws InterruptedException {
		assertMapSafeInThreadPool(3);
	}

	/**
	 * Treeifying bins (where entries with the same key.hashCode goes) is done
	 * after reaching a threshold ({@code HashMap#TREEIFY_THRESHOLD}). Let's try
	 * to hunt the problem which can occur there (a {@link ClassCastException}).
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
	 * Test removing entries from a Map using 2 worker threads. The Map instance
	 * is filled with values first and then 2 worker threads are used to remove
	 * entries from the map. After the worker threads successfully finishes, the
	 * size of the map is verified (0 is expected).
	 */
	@Test
	public void testRemoveWithTwoThreads() throws InterruptedException {
		System.out.println("Starting test which removes from the map  using 2 threads");
		final Map<Integer, Integer> map = createMapInstance();
		for (int i = 0; i < TEST_ENTRIES_COUNT; i++) {
			map.put(i, i);
		}
		final AtomicInteger counter = new AtomicInteger(TEST_ENTRIES_COUNT);
		final Thread[] threads = new Thread[2];
		// create workers
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new RemoveFromMapRunnable(map, counter), "T" + i);
			threads[i].start();
		}
		// wait for all the workers to finish
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
		// check the counter value. (This's not expected to fail.)
		Assert.assertTrue("Unexpected counter size after finishing", counter.get() < 1);
		// if the remove operation would be thread-safe, we would see empty map
		// here.
		Assert.assertEquals("Unexpected Map size after finishing", 0, map.size());
	}

	/**
	 * Implementation of {@link Map} thread safety test with given count of
	 * threads.
	 *
	 * @param threadCount
	 */
	private void assertMapSafeInThreadPool(int threadCount) throws InterruptedException {
		System.out.println("Starting test with filling map using " + threadCount + " thread(s)");
		final Map<Integer, String> map = createMapInstance();
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
		// check if the counter reached the expected size. (This's not expected
		// to fail.)
		final int expectedMapSize = counter.get();
		Assert.assertFalse("Creating entries finished too early", expectedMapSize < TEST_ENTRIES_COUNT);

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
	 * Runnable which adds new entries to given {@link Map}. Newly added keys
	 * are taken from given (shared) counter and as a value is used the current
	 * thread name.
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
				// the internal counter should never be greater than the map
				// size;
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
				Assert.assertEquals(map.remove(removeKey), new Integer(removeKey));
			}
		}
	}

	/**
	 * Thread which adds entries with the same hashcode. The thread runs 1
	 * minute at most.
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
