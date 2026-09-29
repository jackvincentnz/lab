# Signing key

The gateway signs the identity token it forwards to verticals with one RSA key and publishes the
public half at `/.well-known/jwks.json`. Verticals cache keys by `kid` and refetch the set when a
token names a `kid` they do not hold.

## Local development

The `local` profile sets `lab.gateway.token.ephemeral-key`, which generates a key at startup under
a random `kid`. A restart rotates the key, and verticals pick up the new one on the first token
that names it.

## Other environments

Without `lab.gateway.token.ephemeral-key` the gateway refuses to start unless
`lab.gateway.token.private-jwk` holds an RSA private JWK of at least 2048 bits that carries its
`kid`. Generate one, keep the file outside version control, and pass it through the environment:

```zsh
node -e "const {generateKeyPairSync} = require('node:crypto'); const {privateKey} = generateKeyPairSync('rsa', {modulusLength: 2048}); console.log(JSON.stringify({...privateKey.export({format: 'jwk'}), kid: '2026-09', use: 'sig', alg: 'RS256'}))" > gateway-signing-key.json
LAB_GATEWAY_TOKEN_PRIVATE_JWK="$(cat gateway-signing-key.json)" bazel run //projects/gateway
```

Every replica must hold the same key, because a vertical fetches the JWK set from whichever
replica answers.

## Rotation

A new key must carry a new `kid`, or verticals keep verifying against the cached key until it
expires. The published set holds one key, so tokens signed just before a rotation fail until they
expire, at most five minutes. Publishing the outgoing key alongside the new one is not supported.
