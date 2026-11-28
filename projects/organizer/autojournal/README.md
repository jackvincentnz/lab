---
uses: [libs/test]
---

# Autojournal

Service that adds a [Journal](../journal/README.md) entry for each completed
[Task](../task/README.md).

## Development

It needs Kafka, the schema registry, and the Task and Journal services, so run
it with them as the [Organizer setup guide](../README.md#getting-started)
describes.

## Testing

```zsh
bazel test //projects/organizer/autojournal/...
```
