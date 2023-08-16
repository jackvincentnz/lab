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
  - [Spring-kafka transaction synchronization](https://docs.spring.io/spring-kafka/reference/html/#transaction-synchronization)
  - [Distributed transactions in Spring](https://www.infoworld.com/article/2077963/distributed-transactions-in-spring--with-and-without-xa.html)
- [Strict vs dynamic schema design](https://www.confluent.io/blog/spring-kafka-protobuf-part-1-event-data-modeling/)

## Useful resources

- [Topic naming](https://medium.com/@kiranprabhu/kafka-topic-naming-conventions-best-practices-6b6b332769a3#:~:text=Don't%20tie%20topic%20names,dynamic%20and%20changes%20over%20time%20.)
