# Check Map implementation thread safety

These tests **doesn't prove** the tested `Map` implementation **is thread safe**! It can just prove (if some test fail) that given implementation **is not** thread safe.

## Run

```bash
git clone -b hashmap-thread-safety https://github.com/kwart/test-app.git
cd test-app
mvn clean test
```

## Implementation

Abstract parent [AbstractMapTestBase.java](src/test/java/cz/cacek/test/AbstractMapTestBase.java) contains tests logic. The implementing classes just provide `Map` instances:
* [HashMapTest.java](src/test/java/cz/cacek/test/HashMapTest.java)
* [SynchronizedHashMapTest.java](src/test/java/cz/cacek/test/SynchronizedHashMapTest.java)
* [ConcurrentHashMapTest.java](src/test/java/cz/cacek/test/ConcurrentHashMapTest.java)

### Example (copy/paste from AbstractMapTestBase.java)

```java
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
```

## What results should you expect?

All the test methods should pass for `SynchronizedHashMapTest` and `ConcurrentHashMapTest` classes.

There will probably fail 4 of 5 tests in `HashMapTest` - they show the synchronization issues.
The only passing test method should be the `testFillWithSingleThread`.
If more tests is passing for you in `HashMapTest`, try to increase the value of `AbstractMapTestBase.TEST_ENTRIES_COUNT` constant. E.g.

```java
private static final int TEST_ENTRIES_COUNT = 10_000_000;
```

### Example output

```
[INFO] Results:
[INFO] 
[ERROR] Failures: 
[ERROR]   HashMapTest>AbstractMapTestBase.huntForTreeifyingIssues:77 Caught ClassCastException when putting entry to the Map
[ERROR]   HashMapTest>AbstractMapTestBase.testFillWithThreeThreads:52->AbstractMapTestBase.assertMapFillingVisibleForGivenThreadCount:190 Unexpected count of null-valued entries found expected:<0> but was:<360461>
[ERROR]   HashMapTest>AbstractMapTestBase.testFillWithTwoThreads:44->AbstractMapTestBase.assertMapFillingVisibleForGivenThreadCount:190 Unexpected count of null-valued entries found expected:<0> but was:<187610>
[ERROR]   HashMapTest>AbstractMapTestBase.testRemoveWithTwoThreads:131 Unexpected Map size after finishing expected:<0> but was:<243916>
[INFO] 
[ERROR] Tests run: 15, Failures: 4, Errors: 0, Skipped: 0
```
