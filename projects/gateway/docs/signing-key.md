# Signing key

The gateway signs the identity token it forwards to verticals with one RSA key and publishes the
public half at `/.well-known/jwks.json`. The key's `kid` is the RFC 7638 thumbprint of the public
key, so it is never configured: every replica holding the same key publishes it under the same
name, and a new key gets a new name. Verticals cache keys by `kid` and refetch the set when a
token names a `kid` they do not hold.

## Local development

The `local` profile sets `lab.gateway.token.ephemeral-key`, which generates a key at startup. A
restart rotates the key, and verticals pick up the new one on the first token that names it.

## Other environments

Without `lab.gateway.token.ephemeral-key` the gateway refuses to start unless
`lab.gateway.token.private-key` holds a PKCS#8 PEM RSA private key of at least 2048 bits.
Generate one, keep the file outside version control, and pass it through the environment:

```zsh
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out gateway-signing-key.pem
LAB_GATEWAY_TOKEN_PRIVATE_KEY="$(cat gateway-signing-key.pem)" bazel run //projects/gateway
```

Every replica must hold the same key, because a vertical fetches the JWK set from whichever
replica answers.

## Rotation

The published set holds one key, so tokens signed just before a rotation fail until they expire,
at most five minutes. Publishing the outgoing key alongside the new one is not supported.
