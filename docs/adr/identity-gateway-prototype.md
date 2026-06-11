# Identity Gateway Prototype

## Status

Accepted for iterative prototyping.

Date: 2026-06-10
Decision owner: Jack Vincent

This record describes the intended prototype architecture and delivery order. Later-phase decisions
are direction rather than commitments to implement them in the current iteration.

Start with the [identity gateway overview](docs/platform/identity-gateway.md) for diagrams and the concise
architecture narrative. This ADR contains the detailed decisions and implementation constraints.

## Context

Mops currently exposes its React application, GraphQL API, GraphQL WebSocket endpoint, and MCP
endpoint without an authenticated platform boundary. It has a tenant header filter, but no principal
identity, web session, OAuth bearer-token validation, or enforced tenant data isolation.

The platform may eventually expose Mops, Organizer, and other business verticals through shared
identity and ingress infrastructure. The identity boundary must therefore remain independent of
Mops domain authorization.

The prototype should prove browser sessions and programmatic access while remaining small enough
to review and land incrementally.

The prototype has five goals:

1. Add browser login to Mops using sessions.
2. Allow users to authorize external applications to access Mops APIs.
3. Accept bearer tokens on the public API surface.
4. Allow users to authorize external agents to a remote MCP server exposing Mops data.
5. Establish an identity solution that can grow across Mops, Organizer, and other verticals.

## Decision

Create a shared identity gateway under `projects/platform/gateway`. It will be the public entry point
for platform applications and APIs, beginning with Mops.

The gateway, provider adapters, identity mappings, and browser-session authority begin as one
deployable application with internal module boundaries. Mops remains a separate downstream service.
Splitting identity into another network service is deferred until there is an operational reason.

The gateway will:

- Use Spring Boot, Spring Cloud Gateway, and WebFlux.
- Serve one public HTTPS origin behind an AWS load balancer.
- Route verticals using explicit path-based configuration.
- Manage first-party browser sessions.
- Adapt upstream identity-provider subjects to platform identifiers.
- Validate OAuth bearer tokens for API and remote MCP access.
- Sanitize and inject the trusted downstream identity contract.

Mops and other verticals remain private upstream services. They own all domain, object, and
attribute authorization.

[Spring Cloud `2025.1.x`](https://spring.io/projects/spring-cloud/) and
[Spring Cloud Gateway `5.0.x`](https://docs.spring.io/spring-cloud-gateway/reference/index.html)
officially support Spring Boot `4.0.x`. Iteration 3 must still begin with a focused integration spike
because Spring Cloud dependencies and their BOM are not currently present in this Bazel repository.
The spike must prove HTTP, WebSocket, and streaming MCP proxy behavior. If the full Gateway server
cannot be integrated cleanly, the fallback is a smaller Spring WebFlux application using Spring's
proxy support while retaining the same route and filter contracts.

## Provider Portability

The prototype proves code-level provider portability while implementing only one live provider,
WorkOS. It does not attempt simultaneous WorkOS and Keycloak support.

Provider-neutral platform code owns sessions, platform identifiers, downstream identity, route
policy, and vertical configuration. WorkOS-specific SDKs, claims, endpoints, and identifier formats
remain inside adapters for:

- Interactive browser authentication.
- Authorization-server discovery and bearer-token verification.
- External user and organization mapping.
- Logout and provider session coordination.

Replacing WorkOS should require a new adapter and configuration, plus migration of external identity
mappings and OAuth clients. It must not require changes to Mops, the downstream identity contract,
or first-party session semantics. Keycloak is the expected first alternative used to evaluate this
boundary after the WorkOS prototype, but it is not implemented now.

The WorkOS capability spike must leave a short checked-in record of observed claims, metadata,
consent behavior, scope or permission support, and MCP client compatibility. Provider behavior must
not remain knowledge held only by the implementation author.

## Identity Model

The platform owns opaque UUID identifiers:

- The `principal_id` identifies a human or service account independently of an identity provider.
- The `tenant_id` identifies a customer tenant independently of an upstream organization.
- The `principal_type` initially supports `HUMAN` and reserves `SERVICE_ACCOUNT` for later use.

For the prototype, WorkOS is authoritative for users, organizations, and organization memberships.
The identity database stores just-in-time mappings from WorkOS identifiers to platform identifiers.
Mops never stores or receives WorkOS identifiers.

Durable principals and external mappings live in a gateway-owned PostgreSQL database. Redis stores
ephemeral browser sessions only. Mapping creation is idempotent and unique by provider issuer and
external subject or organization identifier.

The schema may support multiple external identities later, but the prototype permits one external
identity per principal and provides no account-linking or merge workflow. Matching email addresses
must never merge principals.

The membership model remains many-to-many even though the initial browser flow supports only users
with one WorkOS organization. A request, session, or OAuth token always has one active tenant. The
prototype rejects ambiguous browser login when a user belongs to multiple organizations rather than
adding tenant-selection UI.

Email domain is not used to infer tenant membership.

## Authentication Flows

### Browser

1. The browser enters through the gateway.
2. The gateway delegates authentication to WorkOS.
3. The callback maps the WorkOS user and organization to platform IDs.
4. The gateway creates a first-party session in Redis.
5. The browser receives an opaque, secure, HTTP-only cookie.
6. The gateway resolves the session and forwards trusted identity context to the selected vertical.

The first-party session, not a WorkOS token, is the browser credential. Upstream access or refresh
tokens are retained only when required for upstream renewal, API access, or coordinated logout.
Logout invalidates the local session before redirecting through WorkOS logout.

Any retained provider refresh token is encrypted and stored server-side outside the Redis session;
the session contains only platform IDs and simple authentication metadata. Browser JavaScript never
receives a bearer token for ordinary application requests.

Initial session defaults are a 24-hour idle timeout and a 7-day absolute lifetime. These may later
be constrained or overridden by tenant policy. The gateway rotates the session ID after login,
tenant switching, privilege elevation, and impersonation. Session-authenticated requests receive a
configured first-party scope set for the selected vertical so downstream scope handling is uniform
for browser sessions and bearer tokens.

The first-party session is platform-wide for every vertical on the shared public origin. It proves
the principal and active tenant but does not imply entitlement to every vertical. Each vertical still
owns its authorization decisions.

Local development uses configured personas that create normal Redis-backed sessions through a
development-only login endpoint. Keycloak may later provide a local OIDC integration profile, but is
not part of the first prototype.

### API And Remote MCP

External clients use OAuth authorization-code flows, including PKCE for public clients. WorkOS is
the initial authorization server and manages consent, grants, access tokens, and refresh tokens.
Clients retain their own refresh tokens.

The gateway validates bearer tokens locally using WorkOS discovery and JWKS, including issuer,
expiry, audience, organization, and scopes. Each vertical has a distinct OAuth audience. The
gateway maps the token subject and organization to platform IDs and forwards normalized identity
context.

The gateway treats vertical scopes as opaque strings. Each vertical defines and enforces its own
scope semantics. MCP execution remains inside Mops and uses the same application authorization as
other Mops entry points.

API keys and service-account credentials are deferred.

## Downstream Identity Contract

The first shared contract contains only:

```http
X-Principal-Id: <uuid>
X-Tenant-Id: <uuid>
X-Scopes: mops:read mops:write
```

Scopes use OAuth's space-delimited, case-sensitive syntax. The gateway removes client-supplied
copies before injecting trusted values.

For the prototype, private networking is the trust boundary between the gateway and downstream
services. Signed internal assertions, workload identity, or mTLS may harden this boundary later
without changing the logical contract.

The shared Java types and servlet extraction support will live in `libs/identity`, replacing
`libs/tenancy`. Mops will use a request-thread-bound context with a required accessor that fails when
identity is absent. Asynchronous work must capture principal and tenant explicitly rather than read
the servlet thread context.

Mops supports two explicit identity modes:

- The `development` mode synthesizes identity from stable configured UUIDs and scopes.
- The `trusted-headers` mode requires valid identity headers and returns `401 Unauthorized` before dispatch
  when they are missing or malformed.

Identity enforcement defaults to `trusted-headers`. Health endpoints are explicitly public. Scope
enforcement is not part of the first identity-contract change.

## Public Routing

The initial public layout uses one host and explicit paths:

```text
/                         Redirect to /mops/
/auth/*                   Platform login, callback, logout, and session operations
/mops/*                   Mops React application
/api/mops/graphql         Mops GraphQL HTTP
/ws/mops/graphql          Mops GraphQL WebSocket
/mcp/mops                 Mops remote MCP
/.well-known/oauth-protected-resource/*
                          OAuth protected-resource metadata
/organizer/*              Possible future Organizer routes
```

The host-only session cookie uses `Path=/`. The gateway proxies frontend traffic to the existing
private Mops Nginx/Vite service rather than packaging React assets into the gateway. The cookie is
`Secure`, `HttpOnly`, and initially `SameSite=Lax`.

Routes default to authenticated and explicitly declare public, browser-session, or bearer-token
access. Cookie-authenticated GraphQL POST requests require CSRF protection. WebSocket upgrades
validate the browser origin and bind principal and tenant for the connection lifetime. Proactive
socket termination after session revocation is deferred to the identity-operations draft.

Public routes are an explicit allowlist covering required static assets, login and callback routes,
health checks, and OAuth metadata. The React client obtains a CSRF value from the gateway and echoes
it in a custom header; the gateway requires it on every cookie-authenticated GraphQL POST because
queries and mutations share the same endpoint.

Moving the Mops frontend under `/mops/` is owned by iteration 3. That change includes the Vite base,
React Router basename and routes, static asset paths, GraphQL and WebSocket URLs, reset endpoint,
Nginx fallback behavior, and affected tests. It is not only a gateway route configuration change.

TLS terminates at the AWS load balancer. The gateway trusts the load balancer's forwarded host,
scheme, and `X-Forwarded-For` headers for the prototype. IP allowlisting is deferred.

## Deferred Design

Identity operations and Mops tenant-data lifecycle are useful future directions, but they are not
required to prove the five prototype goals and are not committed by this ADR:

- [Draft: Identity operations](docs/adr/identity-operations-draft.md) covers profiles, request correlation,
  membership revocation, platform administration, and impersonation.
- [Draft: Mops tenant data lifecycle](docs/adr/mops-tenant-data-lifecycle-draft.md) covers database-per-tenant,
  migrations, provisioning, cloning, and the future control-plane boundary.

## Delivery Plan

Changes should remain independently reviewable and leave the repository runnable after each step.

Before this plan begins, an initial cleanup removed the Mops-owned tenant aggregate and public
provisioning API. This established that Mops does not create canonical tenant IDs and deliberately
left replacement tenant provisioning outside the prototype.

1. **WorkOS capability spike**
   Before making the OAuth model foundational, verify third-party consent, public clients with PKCE,
   custom vertical scopes or their replacement, resource indicators and audiences, organization
   selection, token claims, protected-resource metadata, and representative MCP client behavior.
   Record which behavior is provider capability and which belongs in a platform adapter.
2. **Shared identity contract**
   Replace `libs/tenancy` with `libs/identity`; add platform IDs, request identity, trusted headers,
   development and trusted-header modes, servlet context handling, and test utilities. Incorporate
   the scope and audience conclusions from the WorkOS capability spike.
3. **Gateway skeleton**
   Add configured HTTP, WebSocket, MCP, and frontend routes; strip and inject identity headers using
   one configured development identity. First prove Spring Cloud Gateway integration, then move the
   Mops frontend and client URLs under `/mops/`.
4. **Local sessions**
   Add Redis-backed Spring Session, local personas, login/logout, opaque cookies, and CSRF.
5. **WorkOS browser login**
   Add the provider adapter, authorization-code callback, just-in-time mappings, single-organization
   enforcement, first-party session creation, and coordinated logout.
6. **External application, bearer, and MCP authentication**
   Add WorkOS JWT validation, per-vertical audiences, organization mapping, scope propagation,
   protected-resource metadata, third-party application authorization, and authenticated API and MCP
   proxying.

The prototype is complete after iteration 6 demonstrates all five goals. Logging and profile
projection, membership webhooks and revocation, database-per-tenant, and tenant provisioning require
separate decisions and delivery rounds.

The existing `bazel run //projects/mops` target remains the fast direct development loop initially.
A separate `//projects/mops:with-gateway` target will run the integrated stack. The direct target
must explicitly set `identity.mode=development` with stable configured IDs; the production default
remains `trusted-headers`. Individual app, service, and gateway targets remain available.

Iteration 4 is the first end-to-end browser-session milestone: a user can select a local persona, receive
an opaque Redis-backed session, load Mops through the gateway, make CSRF-protected GraphQL requests,
and log out without WorkOS credentials.

## Identity Contract Acceptance Criteria

Iteration 2 is complete when:

- The `libs/identity` library replaces `libs/tenancy`.
- The shared contract contains `principal_id`, `tenant_id`, and scopes.
- Mops supports explicit `development` and `trusted-headers` modes.
- Protected requests establish and reliably clear request identity.
- Absent or malformed trusted headers return `401 Unauthorized`.
- Health endpoints remain public.
- Tests cover parsing, cleanup, required access, test context, and configuration behavior.
- Existing Mops tests pass.

It does not add the gateway, WorkOS, Redis, profiles, or tenant-data provisioning behavior.

## Prototype Exit Criteria

The prototype is complete when:

- A browser user can authenticate through WorkOS and use Mops through a Redis-backed platform
  session.
- A third-party application can obtain user authorization and call a Mops API with a bearer token.
- Invalid or incorrectly-audienced bearer tokens are rejected at the public boundary.
- A representative external agent can discover authorization metadata, obtain consent, and use the
  remote Mops MCP endpoint.
- Mops receives only provider-neutral principal, tenant, and scope context.
- Adding a second vertical requires route, audience, scope, and first-party-session configuration,
  not a second identity implementation.
- The WorkOS adapter boundary and a plausible Keycloak mapping are documented, without implementing
  Keycloak.

## Alternatives Considered

- **Operate a platform OAuth/OIDC authorization server.** Rejected for the prototype because WorkOS
  already supplies authorization-code flows, consent, token issuance, and MCP support. Platform IDs
  and provider mappings preserve a migration path.
- **Use a custom PostgreSQL-backed WebFlux session repository.** Rejected in favor of standard
  Redis-backed Spring Session, which provides the reactive session lifecycle and horizontal scaling
  needed by the gateway with less custom security-sensitive code.
- **Use one shared Mops schema with a `tenant_id` column.** Rejected because the expected scale is
  hundreds of tenants and database-per-tenant better supports deletion, export, cloning, sandboxing,
  and isolation. It introduces registry, migration, and connection-pool operations that are deferred
  until after the identity slices.
- **Have the gateway issue signed internal JWTs to downstream services.** Deferred because private
  networking is an acceptable prototype trust boundary. Signed assertions, workload identity, or
  mTLS can be added without changing the downstream identity model.
- **Build a custom canonical tenant and membership control plane immediately.** Rejected for the
  prototype to avoid synchronizing a second tenancy system. WorkOS remains authoritative until the
  need for a control plane is proven.
- **Provision Mops on first user access.** Rejected because tenant readiness should not depend on user
  behavior. This remains a draft tenant-lifecycle decision outside the current prototype.

## Consequences

The design keeps identity-provider concepts out of verticals and domain authorization out of the
gateway. WorkOS significantly reduces initial protocol and administration work while stable platform
IDs preserve a path to another provider or authorization server.

The prototype accepts temporary limitations: private-network header trust, WorkOS-owned membership,
one-organization browser login, no API keys, and no tenant-selection UI. Just-in-time mapping also
means any successfully authenticated WorkOS user in the configured WorkOS environment can receive a
platform principal and session. WorkOS organization membership is the prototype tenant-access gate;
application provisioning is addressed separately. These are explicit delivery tradeoffs rather than
permanent data-model constraints.
