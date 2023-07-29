# Learn Kafka

## Getting Started with Apache Kafka and Spring Boot

Based on: https://developer.confluent.io/get-started/spring-boot/#introduction

## How to run

```shell
# Start required infrastructure
cd learn/kafka
docker-compose up -d

# Produce some events
bazel run //learn/kafka -- --producer

# Consume some events
bazel run //learn/kafka -- --consumer
```

## Next steps

- Integration tests example
- Schema registry
- More complicated example hosted in example architecture
