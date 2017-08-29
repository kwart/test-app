# Check HashMap thread safety

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
[ERROR]   HashMapTest>AbstractMapTestBase.huntForTreeifyingIssues:82 Caught ClassCastException when putting entry to the Map
[ERROR]   HashMapTest>AbstractMapTestBase.testFillWithThreeThreads:56->AbstractMapTestBase.assertMapSafeInThreadPool:150 Unexpected count of null-valued entries found expected:<0> but was:<498025>
[ERROR]   HashMapTest>AbstractMapTestBase.testFillWithTwoThreads:48->AbstractMapTestBase.assertMapSafeInThreadPool:150 Unexpected count of null-valued entries found expected:<0> but was:<507767>
[ERROR]   HashMapTest>AbstractMapTestBase.testRemoveWithTwoThreads:114 Unexpected Map size after finishing expected:<0> but was:<242356>
[INFO] 
[ERROR] Tests run: 15, Failures: 4, Errors: 0, Skipped: 0
```
