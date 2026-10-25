---
status: Proposed
date: 2026-09-26
owner: Jack Vincent
affects: [projects/gateway, projects/mops, libs/identity]
---

# Edge Gateway

## Context

Mops, Organizer, and the services that follow are private downstream services. Something must sit
in front of them and answer, for every public request, who the caller is and which tenant they
act for.

Six controls are involved:

1. Delegated authentication of public callers.
2. Browser session management.
3. The identity contract handed to downstream services.
4. Per-tenant IP allowlisting.
5. Access logging.
6. Per-tenant rate limiting.

Each could live in every downstream service or in one shared entry point. Duplicating them means
several implementations of the same security-sensitive logic, drift between them, and a wider
attack surface. This record decides where the controls live and the minimum shape of each.

## Decision

One edge gateway, built on Spring Boot and Spring Cloud Gateway, is the only public entry point.
All six controls live in it. Downstream services receive a small identity contract and own all
domain authorization.

### Routing

The public URL structure is not decided. The first iteration serves Mops at the root of one host
because it is the only service, so a prefix would buy nothing and would force the app to change
its base path. Path prefix per service, subdomain per service, and other layouts remain open.
Health endpoints are public.

### Authentication

The gateway is the only place public credentials are checked, so that private services never see
them. An external identity provider handles sign-in, MFA, social login, and enterprise federation,
so that the platform does not build them. The gateway owns the browser session, so that sessions
behave the same whichever provider signs users in. The provider proof of concept chooses between
standard OIDC and a provider SDK.

API and MCP callers send access tokens from the provider. The gateway checks their issuer,
signature, expiry, and audience. Client registration and consent belong to the provider, so that
the platform does not build them.

- **One audience per product.** The audience is the product's public URL, such as Mops's, never a
  private service. A token for one product is then refused by another, private services can change
  without clients noticing, and the same URL names the MCP resource.
- **MCP shares it.** Mops's API and MCP endpoints are one resource, and third-party MCP clients get
  fewer scopes. MCP gets its own audience only if it needs different token lifetimes, if its
  consent must never grant API access, or if a provider accepts only the exact MCP endpoint URL.

The provider's token names the user and the organization they chose at sign-in or consent, so
that each request acts for exactly one tenant. The gateway maps both to a platform principal and
tenant through a tenant service. It then checks that the principal belongs to the tenant and that
the tenant allows that sign-in method, so that membership stays a platform decision whichever
provider is chosen. A token without an organization gets 401. A valid token for a tenant the user
does not belong to, or a sign-in method the tenant does not allow, gets 403.

The mapping runs once at login for browsers and once per token for API callers. Having the
provider put platform identifiers in its tokens would remove it, and is deferred. The tenant
service is a separate decision.

### Sessions

Browser sessions are server-side, Redis-backed, and referenced by an opaque `HttpOnly`, `Secure`,
`SameSite=Lax` cookie. `Lax` because the provider's login callback is a cross-site navigation.

The cookie is the browser credential. Provider tokens stay in the gateway, so that browser
scripts cannot read them. A session acts for one tenant at a time, so that every request is clear
about whose data it touches. Switching tenant may require signing in again to meet that tenant's
policy. How the session stores this is deferred.

Session requests are CSRF-protected; see the [Gateway CSRF decision](gateway-csrf.md) for token
validation and delivery. WebSocket upgrades are proxied under the same rules.

### Downstream contract

The gateway resolves the caller, mints a short-lived identity token for the service it forwards
to, and sends it as `Authorization: Bearer <jwt>` in place of whatever the client sent. The token
is signed with the gateway's private key and carries:

| Claim               | Value                                                      |
| ------------------- | ---------------------------------------------------------- |
| `iss`               | `lab-gateway`                                              |
| `aud`               | The service, for example `mops`                            |
| `sub`               | Principal UUID                                             |
| `tenant`            | Active tenant UUID                                         |
| `scope`             | Space-separated scopes, for example `mops:read mops:write` |
| `amr`               | How the caller authenticated: `form`, `oidc`, or `bearer`  |
| `iat`, `exp`, `jti` | Issued at, expiry, and a unique token ID                   |

One token grows by adding claims, so the contract extends without a new header per fact. Claims
describe how the caller was authenticated and what tenant they act under, which only the gateway
knows. Anything a service could look up by principal and tenant stays a lookup.

A downstream service verifies the signature, issuer, audience, and expiry against the JWK set the
gateway publishes, and trusts nothing else about the caller. Tokens name their key by `kid`, so
rotating a key is a gateway-only event. The signed token is the trust boundary between gateway and
service; private networking only limits who can present one.

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

One structured record per request: source IP, path, status, and a request ID forwarded to the
service, with `tenant_id`, `principal_id`, and authentication method present when the gateway
authenticated the caller. Tokens and cookies are never logged.

## First Iteration

The first iteration proves the path from public request to downstream service with the fewest
dependencies:

- The gateway authenticates configured in-memory users with Spring form login. Each user carries
  a principal, tenant, and scopes. No external provider, no tenant service.
- API clients send bearer tokens from one configured issuer, a mock OAuth2 server locally, so
  that API access can be tested before a provider is chosen. The audience `lab-gateway` stands in
  for Mops's public URL, and a `tenant` claim stands in for the organization mapping.
- The gateway accepts one audience. It checks the audience per route once a second product is
  added.
- Allowlisting and rate limiting are not enforced.
- Routing to Mops, the signed identity token, CSRF, WebSocket proxying, and access logging are
  all in.

Provider sign-in replaces form login later. The gateway keeps owning the session, so that
downstream services and the identity token do not change.

## Out Of Scope

- Identity provider selection.
- Tenant service design.
- Platform identifiers via lookup versus provider-minted claims.
- Public URL structure, including subdomain per service.
- Hardening gateway-to-service trust beyond the signed token, such as mTLS.
- Per-tenant rate limiting mechanism.
- WAF, DDoS mitigation, and volumetric rate limiting, which belong in front of the load balancer.
- Service-to-service and background-job identity.

## Alternatives Considered

- **Each service implements the controls.** Rejected. Every service would carry its own OIDC
  client, session store, token validation, allowlist, and logging, with as many security postures
  as services.
- **Kong, NGINX, or Envoy.** Rejected for now. They handle routing and bearer validation, but
  tenant resolution and the downstream contract need custom logic, and the team's expertise is
  Spring.
- **Gateway as external authorization server.** Rejected. Issuing tokens to external clients,
  consent, and client management are a large surface the provider already supplies. The gateway
  still mints its own internal identity tokens.
- **Gateway owns the identity mapping.** Rejected. The gateway would grow into an identity system
  with its own admin surface.

## Consequences

Services get authentication, sessions, allowlisting, rate limiting, and access logging without
implementing any of them. Security-sensitive code exists once.

The gateway is on the path of every request and must scale horizontally. Redis is a second
availability dependency for browser sessions. A workload that captures a token can replay it to
the service until it expires, and custody of the gateway's private key decides who can mint one.
The tenant service becomes a login-path dependency once it exists.
