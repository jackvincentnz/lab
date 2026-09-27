# Edge Gateway

## Status

Proposed.

Date: 2026-09-26
Decision owner: Jack Vincent

## Context

Mops, Organizer, and future verticals are private downstream services. Something must sit in front
of them and answer, for every public request, who the caller is and which tenant they act for.

Six controls are involved:

1. Authentication of public credentials.
2. Browser session management.
3. The identity contract handed to verticals.
4. Per-tenant IP allowlisting.
5. Access logging.
6. Per-tenant rate limiting.

Each could live in every vertical or in one shared entry point. Duplicating them means several
implementations of the same security-sensitive logic, drift between them, and a wider attack
surface. This record decides where the controls live and the minimum shape of each.

## Decision

One edge gateway, built on Spring Boot and Spring Cloud Gateway, is the only public entry point.
All six controls live in it. Verticals receive a small identity contract and own all domain
authorization.

### Routing

The public URL structure is not decided. The first iteration serves Mops at the root of one host
because it is the only vertical, so a prefix would buy nothing and would force the app to change
its base path. Path prefix per vertical, subdomain per vertical, and other layouts remain open.
Health endpoints are public.

### Authentication

The gateway is both the OAuth2 client and the OAuth2 resource server. No vertical is either.

- **Browser.** OIDC authorization code flow via Spring Security `oauth2Login`.
- **API clients.** Bearer JWTs validated against the provider's JWKS via Spring Security
  `oauth2ResourceServer`, including per-vertical audience.

After authentication the gateway resolves the caller to platform identifiers. A tenant service
owns principals, tenants, memberships, and per-tenant policy. The default is a lookup against it,
once at login for browser callers and once per token for bearer callers. Having the provider mint
tokens that already carry platform claims is a deferred alternative. The tenant service is a
separate decision.

### Sessions

Browser sessions are server-side, Redis-backed, and referenced by an opaque `HttpOnly`, `Secure`,
`SameSite=Lax` cookie. `Lax` because the provider's login callback is a cross-site navigation.

The cookie is the browser credential. Provider tokens stay in the gateway. A session has one active
tenant and a configured first-party scope set per vertical.

Session requests are CSRF-protected. WebSocket upgrades are proxied under the same rules.

### Downstream contract

The gateway resolves the caller, mints a short-lived identity token for the vertical it forwards
to, and sends it as `Authorization: Bearer <jwt>` in place of whatever the client sent. The token
is signed with the gateway's private key and carries:

| Claim               | Value                                                      |
| ------------------- | ---------------------------------------------------------- |
| `iss`               | `lab-gateway`                                              |
| `aud`               | The vertical, for example `mops`                           |
| `sub`               | Principal UUID                                             |
| `tenant`            | Active tenant UUID                                         |
| `scope`             | Space-separated scopes, for example `mops:read mops:write` |
| `amr`               | How the caller authenticated: `form`, `oidc`, or `bearer`  |
| `sid`               | Gateway session                                            |
| `iat`, `exp`, `jti` | Issued at, expiry, and a unique token ID                   |

One token grows by adding claims, so the contract extends without a new header per fact. Claims
describe how the caller was authenticated and what session and tenant they act under, which only
the gateway knows. Anything a vertical could look up by principal and tenant stays a lookup.

A vertical verifies the signature, issuer, audience, and expiry against the JWK set the gateway
publishes, and trusts nothing else about the caller. Tokens name their key by `kid`, so rotating
a key is a gateway-only event. The signed token is the trust boundary between gateway and
vertical; private networking only limits who can present one.

### Per-tenant IP allowlisting

After authentication, the gateway checks the client IP against the tenant's CIDR allowlist held by
the tenant service. It is a tenant policy, not an edge defence, and tenants that enable it cannot
use clients on dynamic addresses. Enforcement depends on a fixed rule for trusting
`X-Forwarded-For` behind the load balancer.

### Per-tenant rate limiting

Request quotas are tenant policy held by the tenant service and enforced at the gateway after
authentication, the same shape as the allowlist. Volumetric and abuse limiting stays at the edge
in front of the load balancer. The mechanism is a deferred decision.

### Access logging

One structured record per authenticated request: source IP, `tenant_id`, `principal_id`,
authentication method, path, status, and a request ID forwarded to the vertical. Tokens and
cookies are never logged.

## First Iteration

The first iteration proves the path from public request to vertical with the fewest dependencies:

- The gateway authenticates configured in-memory users with Spring form login. Each user carries
  a principal, tenant, and scopes. No external provider, no tenant service.
- Browser sessions only. Bearer tokens are not accepted.
- Fast follow: public bearer JWTs signed with a static gateway key, to test API access before a
  provider exists.
- Allowlisting and rate limiting are not enforced.
- Routing to Mops, the signed identity token, CSRF, WebSocket proxying, and access logging are
  all in.

OIDC login replaces form login later with the same session semantics.

## Out Of Scope

- Identity provider selection.
- Tenant service design.
- Platform identifiers via lookup versus provider-minted claims.
- Public URL structure, including subdomain per vertical.
- Hardening gateway-to-vertical trust beyond the signed token, such as mTLS.
- Per-tenant rate limiting mechanism.
- WAF, DDoS mitigation, and volumetric rate limiting, which belong in front of the load balancer.
- Service-to-service and background-job identity.

## Alternatives Considered

- **Each vertical implements the controls.** Rejected. Every vertical would carry its own OIDC
  client, session store, token validation, allowlist, and logging, with as many security postures
  as verticals.
- **Kong, NGINX, or Envoy.** Rejected for now. They handle routing and bearer validation, but
  tenant resolution and the downstream contract need custom logic, and the team's expertise is
  Spring.
- **Gateway as authorization server.** Rejected. Token issuance, consent, and client management
  are a large surface the identity provider already supplies.
- **Gateway owns the identity mapping.** Rejected. The gateway would grow into an identity system
  with its own admin surface.

## Consequences

Verticals get authentication, sessions, allowlisting, rate limiting, and access logging without
implementing any of them. Security-sensitive code exists once.

The gateway is on the path of every request and must scale horizontally. Redis is a second
availability dependency for browser sessions. A workload that captures a token can replay it to
the vertical until it expires, and custody of the gateway's private key decides who can mint one.
The tenant service becomes a login-path dependency once it exists.
