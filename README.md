# Kafka with Kerberos authentication

```bash
docker compose up

# test producer/consumer
docker exec -it broker bash

#authenticate to kerberos jduke:theduke
kinit jduke
kafka-topics --bootstrap-server broker.kerberos.example:9092 --create --partitions 3 --replication-factor 1 --topic quickstart --command-config /etc/kafka/consumer.properties
kafka-console-producer --bootstrap-server broker.kerberos.example:9092 --topic quickstart --producer.config /etc/kafka/producer.properties
kafka-console-consumer --bootstrap-server broker.kerberos.example:9092 --topic quickstart --consumer.config /etc/kafka/consumer.properties
```
