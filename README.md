# Kafka with Kerberos authentication

```bash
docker compose up

# test producer/consumer
docker exec -it broker bash

#authenticate to kerberos jduke:theduke
echo theduke | kinit jduke
kafka-topics --bootstrap-server broker.kerberos.example:9092 --create --partitions 3 --replication-factor 1 --topic __consumer_offsets --command-config /etc/kafka/consumer.properties
kafka-topics --bootstrap-server broker.kerberos.example:9092 --create --partitions 3 --replication-factor 1 --topic hztest --command-config /etc/kafka/consumer.properties
kafka-console-producer --bootstrap-server broker.kerberos.example:9092 --topic hztest --producer.config /etc/kafka/producer.properties
kafka-console-consumer --bootstrap-server broker.kerberos.example:9092 --topic hztest --consumer.config /etc/kafka/consumer.properties --from-beginning 
```
