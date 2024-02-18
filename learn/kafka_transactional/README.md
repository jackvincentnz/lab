# Kafka Transactions

## Summary

This demonstrates the synchronizing of listener established kafka transactions, with a jdbc transaction.

This is to ensure that we don't send a new messages if the consume failed for some reason. E.g. The database failed to commit.

A typical example of this being necessary is if we observe some event, update some local state, and then send another event informing that the state has been updated for any downstream consumer.

# Getting started

```shell
# Start required infrastructure
cd learn/kafka_transactional
docker compose up -d

# Run application
bazel run
```

## Resources

- Based on - [Examples of Kafka Transactions with Other Transaction Managers](https://docs.spring.io/spring-kafka/reference/tips.html#ex-jdbc-sync).
- [Chaining Kafka and Database Transactions with Spring Boot: An In-Depth Look](https://raphaeldelio.medium.com/chaining-kafka-and-database-transactions-with-spring-boot-an-in-depth-look-2a7e0e4fe57c).
- [Spring Kafka Transactions](https://docs.spring.io/spring-kafka/reference/kafka/transactions.html).
