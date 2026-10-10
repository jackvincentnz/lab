# Tenancy

Holds the current tenant for a request, and a servlet filter that sets it. The
Organizer task service depends on it.

## Usage

| Target                                                 | Provides                       |
| ------------------------------------------------------ | ------------------------------ |
| `//libs/tenancy/src/main/java/lab/libs/tenancy`        | The tenant context and holder. |
| `//libs/tenancy/src/main/java/lab/libs/tenancy/filter` | The servlet filter.            |

## Testing

```zsh
bazel test //libs/tenancy/...
```
