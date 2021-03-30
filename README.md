# ProjectX JMH benchmark

## TCP / loopback results (4.2)

```
# Run complete. Total time: 00:20:43

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark   Mode  Cnt      Score     Error  Units
App.get    thrpt   25  19029.873 ± 148.163  ops/s
App.put    thrpt   25  11285.536 ±  54.292  ops/s
```

## Unix Socket results (5.0-SNAPSHOT)

```
# Run complete. Total time: 00:21:36

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark   Mode  Cnt      Score     Error  Units
App.get    thrpt   25  22897.663 ± 504.849  ops/s
App.put    thrpt   25  14595.522 ± 284.377  ops/s

```
