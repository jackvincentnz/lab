# Kafka

This is a simple application that can accept messages to be published and consumed as args.

For example. The following will publish three messages "one", "two", and "three", and then consume them:

```shell
# Ensure kafka is running
bazel run //:start

# Run the application
bazel run //learn/kafka -- one two three
```

> [!IMPORTANT]
> Kafka must be running before the application is started.
