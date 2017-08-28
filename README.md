# Check HashMap thread safety

## Run

```bash
git clone -b hashmap-thread-safety https://github.com/kwart/test-app.git
cd test-app
mvn clean install
```

## Implementation

[HashMapTest.java](src/test/java/org/jboss/test/HashMapTest.java)

## What results should you expect?

Probably 4 of 5 tests failing. The `testFillWithSingleThread` should pass always.

If more tests is passing, try to increase the value of `TEST_ENTRIES_COUNT` constant. E.g.

```java
private static final int TEST_ENTRIES_COUNT = 10_000_000;
```  
