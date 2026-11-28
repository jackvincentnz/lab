---
uses: [libs/ddd]
---

# Journal

GraphQL service for Organizer journal entries.

## Development

It needs no other service, so run it on its own from the repository root, at
`http://localhost:3003/graphql` unless `ORGANIZER_JOURNAL_PORT` is set. The
[Organizer setup guide](../README.md#getting-started) runs it with the others.

```zsh
bazel run //projects/organizer/journal
```

## Testing

```zsh
bazel test //projects/organizer/journal/...
```
