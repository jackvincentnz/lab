# Messaging

Writes and reads event metadata as Kafka headers named by the CloudEvents Kafka
binding. The Organizer task and autojournal services depend on it.

## Usage

| Target                                              | Provides                                        |
| --------------------------------------------------- | ----------------------------------------------- |
| `//libs/messaging/src/main/java/lab/libs/messaging` | The `ce_id`, `ce_type` and `ce_source` headers. |

## Testing

```zsh
bazel test //libs/messaging/...
```
