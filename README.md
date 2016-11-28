# KeyManagerFactory synchronization issue reproducer (IBM JDK)


## About the issue

*From description of https://issues.jboss.org/browse/JBEAP-7523*

Our debugging suggests the problem is located in IBM JDK implementation of `javax.net.ssl.KeyManagerFactorySpi` (class `com.ibm.jsse2.ae$a`)

The workflow:

1. user calls `keyManagerFactory.init(keyStore, keystorePassword)` which invokes `com.ibm.jsse2.ae$a.engineInit(Keystore keyStore, char[] password)`;

1. the password (from the second method parameter) is stored into static field `com.ibm.jsse2.ae.d` and in the next step the field is used as parameter for creating new object `new com.ibm.jsse2.aw(keyStore, d)`;

1. the previous step is not synchronized and when more threads call `keyManagerFactory.init()` with different passwords, wrong password may be used for retrieving a key from keystore.

## Reproducer

The reproducer is a simple JUnit test (class `org.jboss.test.KeyManagerTest`). There are 2 test methods:

1. multithreaded test which tries to init KeyManagerFactory instances in a loop in given time interval

1. simple check if the Java is IBM JDK

### How to run it?

```bash
mvn clean test
```

### What does it do?
During `generate-test-resources` maven phase 2 keystores are generated in `target` directory: `keystore1` and `keystore2`. 
Each of them contains a generated keypair. Keystore password and key password are the same as the keystore filename.
  
The multithreaded test uses then 2 threads to init `KeyManagerFactory` for these 2 different keystores.

```java
KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
kmf.init(keyStore, password);
```

It runs in a loop 30 seconds, which seems to be enough time to hit the synchronization problem.

If you run the test with Oracle JDK or OpenJDK then the multithreaded test should work and only the check for IBM JDK should fail.