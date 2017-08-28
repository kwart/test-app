# Check HashMap thread safety

## Run

```bash
git clone -b hashmap-thread-safety https://github.com/kwart/test-app.git
cd test-app
mvn clean install
```

## Implementation

[HashMapTest.java](src/test/java/cz/cacek/test/HashMapTest.java)

## What results should you expect?

Probably 4 of 5 tests failing. The `testFillWithSingleThread` should pass always.

If more tests is passing, try to increase the value of `TEST_ENTRIES_COUNT` constant. E.g.

```java
private static final int TEST_ENTRIES_COUNT = 10_000_000;
```

### Example output

```
[INFO] 
[INFO] Results:
[INFO] 
[ERROR] Failures: 
[ERROR]   HashMapTest.huntForTreeifyingIssues:69 Caught ClassCastException when putting entry to the HashMap
[ERROR]   HashMapTest.testFillWithThreeThreads:44->assertMapSafeInThreadPool:133 Unexpected count of null-valued entries found expected:<0> but was:<236186>
[ERROR]   HashMapTest.testFillWithTwoThreads:36->assertMapSafeInThreadPool:133 Unexpected count of null-valued entries found expected:<0> but was:<423288>
[ERROR]   HashMapTest.testRemoveWithTwoThreads:99 Unexpected HashMap size after finishing expected:<0> but was:<224044>
[INFO] 
[ERROR] Tests run: 5, Failures: 4, Errors: 0, Skipped: 0

```
