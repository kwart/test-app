# ProjectX JMH benchmark

## Docker

### TCP results (4.2)

```
# Run complete. Total time: 00:23:14

Benchmark   Mode  Cnt      Score     Error  Units
App.get    thrpt   25  11623.974 ± 149.088  ops/s
App.put    thrpt   25   6365.013 ± 266.524  ops/s
```

### Unix Domain Sockets results (5.0-SNAPSHOT)

```
# Run complete. Total time: 00:27:46

Benchmark   Mode  Cnt      Score    Error  Units
App.get    thrpt   25  11683.075 ± 94.999  ops/s
App.put    thrpt   25   7344.877 ± 52.752  ops/s

```

### Setup notes

```bash
cat <<EOT >hazelcast.yaml
hazelcast:
  network:
   join:
    multicast:
     enabled: false
    tcp-ip:
     enabled: true
     member-list:
     - 172.17.0.2:5701
EOT

# 3x following
docker run -it --rm -v /home/simulator/hazelcast.yaml:/opt/hazelcast/hazelcast.yaml devopshazelcast/hazelcast-snapshot:af_unix java --add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED -jar hazelcast-4.2.jar

docker run -it --rm devopshazelcast/hazelcast-snapshot:af_unix java --add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED -jar test-app-4.2.jar

# kill all the containers

# 3x following
docker run -it --rm -v /home/simulator/tmp:/mnt devopshazelcast/hazelcast-snapshot:af_unix java --add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED -Dhazelcast.unix.socket.dir=/mnt -jar hazelcast.jar

docker run -it --rm -v /home/simulator/tmp:/mnt devopshazelcast/hazelcast-snapshot:af_unix java --add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED  -Dhazelcast.unix.socket.dir=/mnt -jar test-app-5.0-SNAPSHOT.jar

```

## Single machine

3 Hazelcast members + 1 lite member started in a single JVM.

### TCP / loopback results (4.2)

```
# Run complete. Total time: 00:20:43

Benchmark   Mode  Cnt      Score     Error  Units
App.get    thrpt   25  19029.873 ± 148.163  ops/s
App.put    thrpt   25  11285.536 ±  54.292  ops/s
```

### Unix Socket results (5.0-SNAPSHOT)

```
# Run complete. Total time: 00:21:36

Benchmark   Mode  Cnt      Score     Error  Units
App.get    thrpt   25  22897.663 ± 504.849  ops/s
App.put    thrpt   25  14595.522 ± 284.377  ops/s

```
