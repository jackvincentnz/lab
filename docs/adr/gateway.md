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

The gateway is the platform's public authentication and access-control boundary, so that private
services never handle public credentials. It delegates credential verification, MFA, social login,
and enterprise SAML/OIDC federation to an external identity provider, so that the platform does
not build or operate those features. The gateway owns the authenticated browser session, so that
session lifetime, logout, and tenant switching stay the same whichever provider signs users in.
The protocol, SDK, and session integration are left to the provider proof of concept.

For API and MCP callers the gateway is the OAuth resource server. It validates the access token's
issuer, signature, expiry, and audience. The audience names one public product, never a private
service, so that a token issued for one product cannot be used against another and services can
be split or merged without changing what clients request. A product is identified by its public
URL, such as Mops's, so that the same identifier serves as the MCP resource. Mops's API and MCP
endpoints are one resource, and third-party MCP clients get a narrower scope set rather than a
separate audience, so that one validation path covers both. MCP gets its own audience if it needs
different token lifetimes, if its consent must never grant API access, or if a provider accepts
only the exact MCP endpoint URL as the resource. Client registration and consent belong to the
authorization server, so that the platform does not build client management or consent screens.

After authentication the gateway resolves the caller to a platform principal and tenant, because
provider user and organization identifiers are not the platform's. The token names the tenant:
the provider puts the organization the user chose at sign-in or consent in the token, so that each
token or session acts for one tenant and the gateway never guesses between memberships. The gateway
maps the provider user and organization to a platform principal and tenant through a tenant
service, once at login for browser callers and once per token for bearer callers. It then checks
that the principal belongs to the tenant and that the tenant allows the sign-in method, so that
membership and tenant policy stay platform decisions whichever provider is chosen. A token without
an organization gets 401. A valid token for a tenant the principal does not belong to, or a sign-in
method the tenant does not allow, gets 403. Having the provider mint tokens that already carry
platform identifiers is a deferred alternative that would remove the mapping. The tenant service
is a separate decision.

### Sessions

Browser sessions are server-side, Redis-backed, and referenced by an opaque `HttpOnly`, `Secure`,
`SameSite=Lax` cookie. `Lax` because the provider's login callback is a cross-site navigation.

The cookie is the browser credential. Provider tokens stay in the gateway, so that browser
scripts can never read or replay them. A session has one active tenant, so that every request is
unambiguous about whose data it touches. A principal may belong to multiple tenants, and switching
tenants may require additional authentication to satisfy that tenant's policy. Account linking and
authentication methods use provider features where practical, so that the platform does not build
identity linking. Tenant membership and platform authorization remain platform-owned. The session
representation and policy mechanism are deferred.

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
- API clients send bearer JWTs from one configured issuer, validated against its key set. Tokens
  must name `lab-gateway` as their audience, which stands in for Mops's public URL, and carry the
  platform tenant directly, which stands in for the organization mapping. Locally a mock OAuth2
  server is the issuer, so that API access can be tested before a provider is chosen.
- The gateway accepts exactly one audience. The check becomes per route when a second product is
  added.
- Allowlisting and rate limiting are not enforced.
- Routing to Mops, the signed identity token, CSRF, WebSocket proxying, and access logging are
  all in.

Delegated authentication replaces form login later, and the gateway keeps owning the session, so
that downstream services and the identity token do not change. The integration may use standard
OIDC or a provider SDK.

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
- **Gateway as external authorization server.** Rejected. External OAuth access-token issuance,
  consent, and client management are a large surface the provider already supplies. The gateway
  still issues its own short-lived internal service identity tokens.
- **Gateway owns the identity mapping.** Rejected. The gateway would grow into an identity system
  with its own admin surface.

## Consequences

Services get authentication, sessions, allowlisting, rate limiting, and access logging without
implementing any of them. Security-sensitive code exists once.

The gateway is on the path of every request and must scale horizontally. Redis is a second
availability dependency for browser sessions. A workload that captures a token can replay it to
the service until it expires, and custody of the gateway's private key decides who can mint one.
The tenant service becomes a login-path dependency once it exists.
