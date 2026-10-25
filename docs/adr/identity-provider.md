---
status: Proposed
date: 2026-10-17
owner: Jack Vincent
affects: [projects/gateway]
---

# Identity Provider

## Context

The [edge gateway](gateway.md) is the only public entry point. It owns application sessions,
public-token validation, access controls, and the internal identity-token contract. Private
services do not integrate with an external identity provider. Today the gateway uses form login
and accepts bearer tokens from one configured issuer, a mock server locally. It needs a hosted or
self-managed provider to authenticate users and issue access tokens to external clients.

The provider is an authentication broker and external OAuth authorization server, **not** the
source of truth for internal services, platform tenant membership, or product authorization, so
that changing provider never moves platform data. One platform user may belong to multiple
tenants. Tenants may eventually support different authentication methods and requirements,
including guest access. Prefer provider capabilities for federation, account linking, and
authentication policies, so that the platform does not build and secure them itself. The gateway
still enforces platform access policy.

## Requirements

### Needed for the first integration

1. A straightforward, secure way to authenticate browser users and establish a gateway-owned
   application session. Standard OIDC and provider-supported SDK approaches are both acceptable;
   integration with Spring Cloud Gateway / WebFlux must be verified.
2. Issue externally verifiable OAuth access tokens for public API and MCP resources. The gateway
   validates the public audience and exchanges the external caller context for its own
   service-specific internal token. **No API registration per internal service**, so that private
   service topology never reaches the provider or the consent screen.
3. Put the organization the user chose at sign-in or consent in issued tokens, including tokens
   from MCP consent, so that the gateway can map each token to one platform tenant.
4. Support MCP OAuth authorization, including authorization code with PKCE, resource indicators
   and audience restriction, and practical client identification/registration through CIMD and/or
   DCR as appropriate for supported MCP clients.
5. Show meaningful user consent for third-party delegated access, including the requesting client
   and understandable public permissions/scopes, so that users know what they grant. Internal
   service scopes do not appear in consent.
6. Be viable for a small lab with minimal operational burden and understandable growth pricing.

### Design for, without implementing everything now

- Enterprise SAML/OIDC federation and social authentication, potentially multiple connections
  for one tenant.
- Secure linking of authentication identities to a stable platform principal. Do not link users
  solely because email addresses match, so that someone who controls a matching address at another
  identity provider cannot take over an account.
- Multi-tenant membership, guest users, and tenant-specific authentication requirements.
  Provider-managed organizations select the tenant for a token, but the platform's tenant service
  remains authoritative for membership and product permissions and checks every mapping.
- Sufficient trusted authentication context for the gateway to enforce tenant access policy,
  including possible step-up or reauthentication on tenant switching.

The public resource identifiers and consent scopes are external contracts, independent of private
service topology, so that services can change without breaking clients or consent. The
[gateway ADR](gateway.md) sets one audience per product, identified by its public URL, with the
conditions under which MCP gets its own.

## Decision

**Provider selection remains open.** Auth0, WorkOS AuthKit, and self-hosted Keycloak are the
principal candidates. The previous Auth0 preference relied partly on one Auth0 API per internal
service and documented consent text for those service scopes. That is not the desired boundary,
so it does not establish a decision under the revised criteria.

Each candidate gets the same proof of concept before one is chosen, so that the choice rests on
demonstrated behaviour rather than documentation. The proof covers browser session integration,
a third-party MCP authorization and consent flow, public-resource token validation, and mapping
an external user and organization to a platform principal and tenant. No custom identity-linking
or tenant-policy engine is required for it.

## Candidates and questions to validate

- **Auth0.** Hosted OIDC, social and enterprise connections, organizations, OAuth APIs, consent,
  and documented MCP-related client registration features. Verify practical CIMD/DCR onboarding,
  resource-indicator behaviour for public MCP endpoints, WebFlux integration, and pricing for
  enterprise connections and organizations. Admin registration of CIMD clients may be a
  limitation for arbitrary external MCP clients.
- **WorkOS AuthKit.** Hosted authentication, enterprise SSO, user management, and MCP-oriented
  OAuth support make it a serious candidate. Verify the exact third-party consent UX and scope
  descriptions, secure account linking, gateway session integration for Spring/WebFlux, and
  pricing of the capabilities actually needed. Do not assume a Java SDK covers this gateway.
- **Keycloak.** Self-hosted control and standards-based OIDC, federation, client scopes, and
  consent. Verify maturity and interoperability of CIMD, DCR, and RFC 8707 resource indicators
  for MCP clients. Operating, securing, upgrading, and backing up the service is a real cost.

Other options considered in the earlier evaluation include Clerk, Zitadel, Ory, Cognito,
Microsoft Entra External ID, FusionAuth, and Okta. Revisit only if the principal candidates
cannot satisfy the requirements; earlier exclusions were partly based on the superseded
per-service-audience criterion.

## Consequences

- External authentication and OAuth consent can be provided off the shelf; the gateway retains
  browser session responsibility, public API security enforcement, routing, IP allowlisting,
  rate limiting, and internal service-token issuance.
- The tenant service owns memberships, product authorization, and tenant-specific policy data.
  Provider identity and organization features can be used without making them authoritative for
  the platform's domain model.
- Public OAuth scopes and audiences can evolve independently of internal services.
- Provider-specific SDKs and identity features may introduce switching costs; avoiding a bespoke
  identity system takes priority over speculative portability.
- No provider or final session integration is chosen by this proposed ADR.

## Resolve before accepting

1. Demonstrate a browser login and session integration compatible with Spring Cloud Gateway.
2. Demonstrate an external MCP client completing authorization code + PKCE, public resource
   audience validation, and meaningful consent without per-service API registrations.
3. Demonstrate the organization claim in tokens from browser sign-in and from MCP consent, mapped
   to a platform tenant.
4. Verify registration friction (CIMD/DCR), federation and account-linking capabilities, and
   actual pricing for a small lab and a multi-tenant product.
5. Record the provider choice and concrete tradeoffs in this ADR after the proof.

## References

- [Gateway ADR](gateway.md).
- [MCP authorization, 2026-07-28](https://modelcontextprotocol.io/specification/2026-07-28/basic/authorization).
- [Auth0 pricing](https://auth0.com/pricing).
- [Auth0 resource parameter compatibility](https://auth0.com/ai/docs/mcp/guides/resource-param-compatibility-profile).
- [Auth0 third-party consent](https://auth0.com/docs/get-started/applications/confidential-and-public-applications/user-consent-and-third-party-applications).
- [Auth0 CIMD registration](https://auth0.com/docs/get-started/auth0-overview/create-applications/register-applications-with-cimd).
- [WorkOS AuthKit MCP](https://workos.com/docs/authkit/mcp).
- [WorkOS pricing](https://workos.com/pricing).
- [Keycloak as MCP authorization server](https://www.keycloak.org/securing-apps/mcp-authz-server).
