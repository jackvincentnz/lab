# Bearer tokens

API clients can call the gateway with `Authorization: Bearer <jwt>` instead of a session. The
gateway verifies the token against the key set its configured issuer publishes and forwards the
same identity token to the downstream service that a session produces, with `amr` set to
`bearer`. Bearer requests need no CSRF token and create no session. An invalid or expired token
gets `401`.

## Token contract

| Claim    | Value                                                      |
| -------- | ---------------------------------------------------------- |
| `iss`    | The configured issuer URI                                  |
| `aud`    | Contains `lab-gateway`                                     |
| `sub`    | Principal UUID                                             |
| `tenant` | Active tenant UUID                                         |
| `scope`  | Space-separated scopes, for example `mops:read mops:write` |
| `exp`    | Expiry                                                     |

The audience keeps an identity token minted for a downstream service from being accepted as a
client credential.

## Local issuer

The `local` profile trusts the mock issuer that `compose.yaml` runs on port 3007, or
`GATEWAY_ISSUER_PORT` when set, which Spring Boot starts together with Redis. A `client_credentials` request returns a token for the local `admin`
user's principal and tenant, valid for five minutes:

```zsh
token=$(curl -s -d grant_type=client_credentials -d client_id=lab-cli -d client_secret=unused \
  http://localhost:${GATEWAY_ISSUER_PORT:-3007}/lab/token | jq -r .access_token)
```

Call the API with it:

```zsh
curl -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"query":"{ __typename }"}' http://localhost:3006/api/graphql
```

## Another issuer

Set the issuer and its key set through Spring Boot's resource server properties:

```zsh
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://issuer.example.com \
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://issuer.example.com/jwks \
  bazel run //projects/gateway
```

The issuer must put the gateway in `aud` and issue the `sub`, `tenant`, and `scope` claims above.
Without an issuer configured the gateway does not start.
