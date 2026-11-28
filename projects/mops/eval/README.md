---
uses: [libs/test]
---

# Mops eval

Runner that scores the Mops service's answers to the questions in
[`evals.json`](src/main/resources/evals.json).

## Development

It sends questions to `http://localhost:${MOPS_PORT:-8080}/chats`, so start the
service first, as the [Mops setup guide](../README.md#getting-started)
describes. Both the service and the scorer need `GEMINI_API_KEY`.

## Testing

```zsh
bazel test //projects/mops/eval/...
```
