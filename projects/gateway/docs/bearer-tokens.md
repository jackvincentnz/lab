# Bearer tokens

API clients can call the gateway with `Authorization: Bearer <jwt>` instead of a session. The
gateway verifies the token against one static RSA public key and forwards the same identity token
to the downstream service that a session produces, with `amr` set to `bearer`. Bearer requests
need no CSRF token and create no session. A missing, invalid, or expired token gets `401`.

The bearer key is a separate key pair from the [signing key](signing-key.md). The gateway holds
only its public half, and accepts only tokens issued as `lab-bearer` for the audience
`lab-gateway`, so an identity token minted for a downstream service is never accepted as a client
credential.

## Token contract

| Claim    | Value                                                      |
| -------- | ---------------------------------------------------------- |
| `iss`    | `lab-bearer`, or `lab.gateway.bearer.issuer`               |
| `aud`    | `lab-gateway`, or `lab.gateway.bearer.audience`            |
| `sub`    | Principal UUID                                             |
| `tenant` | Active tenant UUID                                         |
| `scope`  | Space-separated scopes, for example `mops:read mops:write` |
| `exp`    | Expiry, in seconds since the epoch                         |

The token is signed with RS256. `iat` and `nbf` are optional.

## Key

The `local` profile sets `lab.gateway.bearer.ephemeral-key`, which generates a key pair at startup
and logs its private half after `Generated a throwaway bearer key`. Copy the PEM block from the
log into `bearer-private.pem` to mint tokens for that run. A restart generates a new key.

Anywhere else, the gateway refuses to start unless `lab.gateway.bearer.public-key` holds an X.509
PEM RSA public key of at least 2048 bits. Supplying one under the `local` profile keeps the key
across restarts. Generate a key pair, keep both files outside version control, and pass the public
half through the environment:

```zsh
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out bearer-private.pem
openssl pkey -in bearer-private.pem -pubout -out bearer-public.pem
LAB_GATEWAY_BEARER_PUBLIC_KEY="$(cat bearer-public.pem)" bazel run //projects/gateway
```

## Mint a token

Encode the header and claims as base64url, sign them with the private key, and join the three
parts with dots. The principal and tenant below are the local `admin` user's, so the token reaches
Mops as that user. The token expires after five minutes.

```zsh
b64url() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }
now=$(date +%s)
header=$(printf '{"alg":"RS256","typ":"JWT"}' | b64url)
claims=$(printf '{"iss":"lab-bearer","aud":"lab-gateway","sub":"%s","tenant":"%s","scope":"%s","iat":%d,"exp":%d}' \
  11111111-1111-1111-1111-111111111111 22222222-2222-2222-2222-222222222222 \
  "mops:read mops:write" "$now" "$((now + 300))" | b64url)
signature=$(printf '%s.%s' "$header" "$claims" | openssl dgst -sha256 -sign bearer-private.pem -binary | b64url)
token="$header.$claims.$signature"
```

Call the API with it:

```zsh
curl -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"query":"{ __typename }"}' http://localhost:3006/api/graphql
```
