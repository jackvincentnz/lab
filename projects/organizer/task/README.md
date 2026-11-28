---
uses: [libs/ddd, libs/tenancy, libs/test]
---

# Task

GraphQL service for Organizer tasks.

## Development

It needs Kafka and the schema registry, so run it with the other services as the
[Organizer setup guide](../README.md#getting-started) describes.

## Testing

```zsh
bazel test //projects/organizer/task/...
```
