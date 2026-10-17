# Identity

The identity contract between the gateway and downstream services: the
caller's `Identity`, the signed identity token, and servlet filters that put
the identity in scope for a request. The gateway and Mops depend on it.

## Usage

| Target                                                                     | Provides                                       |
| -------------------------------------------------------------------------- | ---------------------------------------------- |
| `//libs/identity/src/main/java/lab/libs/identity`                          | `Identity` and the per-request holder.         |
| `//libs/identity/src/main/java/lab/libs/identity/jwt`                      | Identity token claims, validation, decoding.   |
| `//libs/identity/src/main/java/lab/libs/identity/web`                      | Servlet filters that set the request identity. |
| `//libs/identity/src/test/java/lab/libs/identity/testing:testing-test-lib` | Test tokens and a local JWK set server.        |

## Testing

```zsh
bazel test //libs/identity/...
```
