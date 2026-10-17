# DDD

Domain-driven design building blocks: aggregates, domain events, typed IDs and
value objects, repositories, and an event store. Mops and the Organizer journal
and task services build their domain models on it.

## Usage

| Target                                                            | Provides                                     |
| ----------------------------------------------------------------- | -------------------------------------------- |
| `//libs/ddd/src/main/java/lab/libs/ddd/domain`                    | Aggregates, events, IDs, and value objects.  |
| `//libs/ddd/src/main/java/lab/libs/ddd/domain/springdata`         | Spring Data converters for IDs and values.   |
| `//libs/ddd/src/main/java/lab/libs/ddd/persistence`               | In-memory aggregate store.                   |
| `//libs/ddd/src/main/java/lab/libs/ddd/es/persistence`            | Postgres event store with its `schema.sql`.  |
| `//libs/ddd/src/test/java/lab/libs/ddd/domain/test:test-test-lib` | Test helpers for asserting aggregate events. |

## Testing

```zsh
bazel test //libs/ddd/...
```

`EventRepositoryTest` is tagged `requires-docker` and starts Postgres with
Testcontainers.
