# Reproducer for HandshakeStatus==NEED_TASK issue in netty

Sometimes the OpenSSL engine fails to properly handle delegated tasks.
This project contains a reproducer for the issue.
The cause of problems is probably a thread-safety issue in the `io.netty.handler.ssl.ReferenceCountedOpenSslEngine` class.

Executing the delegated tasks in a blocking manner seems to work correctly.

If the tasks are executed in external threads the processing sometimes fails. The handshake "hangs" in the `NEED_TASK` state.

## Environment

We tested this on Linux OpenJDK versions 8, 11, 17. Most often we hit the problem on Java 11, less often on 8 and sometimes
on 17.

```
$ j8
openjdk version "1.8.0_312"
OpenJDK Runtime Environment (Zulu 8.58.0.13-CA-linux64) (build 1.8.0_312-b07)
OpenJDK 64-Bit Server VM (Zulu 8.58.0.13-CA-linux64) (build 25.312-b07, mixed mode)
$ j11
openjdk version "11.0.13" 2021-10-19 LTS
OpenJDK Runtime Environment Zulu11.52+13-CA (build 11.0.13+8-LTS)
OpenJDK 64-Bit Server VM Zulu11.52+13-CA (build 11.0.13+8-LTS, mixed mode)
$ j17
openjdk version "17" 2021-09-14 LTS
OpenJDK Runtime Environment Zulu17.28+13-CA (build 17+35-LTS)
OpenJDK 64-Bit Server VM Zulu17.28+13-CA (build 17+35-LTS, mixed mode, sharing)
```

## Steps to reproduce

There is an `App` class which executes the test scenario once. There is also a JUnit test class `AppTest`, which executes the
`App` 100 times in a row.

```bash
#!/bin/bash

# repeat the AppTest execution until it fails
i=1; while mvn test; do echo "Starting build $((++i))"; done; echo "Failed $i"
```

### Expected output

```
2022-03-02-10:17:20 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_UNWRAP
Executing tasks, size=1
2022-03-02-10:17:20 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_TASK
Task completed
2022-03-02-10:17:20 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_UNWRAP
2022-03-02-10:17:20 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_UNWRAP
Executing tasks, size=1
2022-03-02-10:17:20 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_TASK
2022-03-02-10:17:20 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_UNWRAP
Task completed
2022-03-02-10:17:20 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NOT_HANDSHAKING
BUFFER_UNDERFLOW and NOT_HANDSHAKING
2022-03-02-10:17:20 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NOT_HANDSHAKING
2022-03-02-10:17:20 [INFO] cz.cacek.test.App run Handshake finished
***************************************************************************************
**********   FINISHED WITH NON-BLOCKING EXECUTOR
***************************************************************************************
```

### Actual output (when the problem is hit)

```
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_UNWRAP
Executing tasks, size=1
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_TASK
Task completed
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_UNWRAP
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_UNWRAP
Executing tasks, size=1
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_TASK
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_UNWRAP
Executing tasks, size=0
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_TASK
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_UNWRAP
Task completed
Executing tasks, size=0
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_TASK
2022-03-02-10:26:33 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_UNWRAP

...

Executing tasks, size=0
2022-03-02-10:20:10 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_TASK
2022-03-02-10:20:10 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_UNWRAP
Executing tasks, size=0
2022-03-02-10:20:10 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_TASK
2022-03-02-10:20:10 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_UNWRAP
Executing tasks, size=0
2022-03-02-10:20:10 [INFO] cz.cacek.test.TLSHandshaker run Client handshake=NEED_TASK
2022-03-02-10:20:10 [INFO] cz.cacek.test.TLSHandshaker run Server handshake=NEED_UNWRAP
2022-03-02-10:20:10 [SEVERE] cz.cacek.test.App run Handshake failed
java.lang.IllegalStateException: We're out of luck. The handshake times out.
    at cz.cacek.test.App.run(App.java:45)
    at cz.cacek.test.App.main(App.java:31)
    at cz.cacek.test.AppTest.test(AppTest.java:10)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:498)
    at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
    at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
    at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
    at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
    at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
    at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
    at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
    at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
    at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
    at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
    at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
    at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
    at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
    at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
    at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
    at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
    at org.apache.maven.surefire.junit4.JUnit4Provider.execute(JUnit4Provider.java:252)
    at org.apache.maven.surefire.junit4.JUnit4Provider.executeTestSet(JUnit4Provider.java:141)
    at org.apache.maven.surefire.junit4.JUnit4Provider.invoke(JUnit4Provider.java:112)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:498)
    at org.apache.maven.surefire.util.ReflectionUtils.invokeMethodWithArray(ReflectionUtils.java:189)
    at org.apache.maven.surefire.booter.ProviderFactory$ProviderProxy.invoke(ProviderFactory.java:165)
    at org.apache.maven.surefire.booter.ProviderFactory.invokeProvider(ProviderFactory.java:85)
    at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:115)
    at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:75)

2022-03-02-10:20:10 [INFO] cz.cacek.test.App run Handshake finished
***************************************************************************************
**********   FINISHED WITH NON-BLOCKING EXECUTOR
***************************************************************************************
```