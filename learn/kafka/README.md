# Learn Kafka

## Getting Started with Apache Kafka and Spring Boot

Based on:

- https://developer.confluent.io/get-started/spring-boot/#introduction
- https://dzone.com/articles/how-to-use-protobuf-with-apache-kafka-and-schema-r

## How to run

```shell
# Start required infrastructure
cd learn/kafka
docker-compose up -d

# Setup topic
docker compose exec broker \
  kafka-topics --create \
    --topic purchases \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1

# Produce some events
bazel run //learn/kafka -- --producer

# Consume some events
bazel run //learn/kafka -- --consumer

# Delete schema versions
curl -X DELETE localhost:8081/subjects/purchases-value
```

## Next steps

- [Integration tests example](https://stackoverflow.com/a/75389041)
- Transaction synchronization
  - [https://docs.spring.io/spring-kafka/reference/html/#transaction-synchronization](https://docs.spring.io/spring-kafka/reference/html/#transaction-synchronization)
  - [https://www.infoworld.com/article/2077963/distributed-transactions-in-spring--with-and-without-xa.html](https://www.infoworld.com/article/2077963/distributed-transactions-in-spring--with-and-without-xa.html)
- More complicated example hosted in example architecture
- [Strict vs dynamic schema design](https://www.confluent.io/blog/spring-kafka-protobuf-part-1-event-data-modeling/)
