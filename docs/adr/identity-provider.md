---
status: Proposed
date: 2026-10-17
owner: Jack Vincent
affects: [projects/gateway]
---

# Identity Provider

## Context

The [edge gateway](gateway.md) is the only public entry point. It owns sessions, checks public
tokens, and mints the identity token that private services trust. Today it uses form login and
accepts bearer tokens from one configured issuer, a mock server locally. It needs a real provider
to sign users in and to issue access tokens to external clients.

The provider signs people in and authorizes external clients. It does not own tenant membership or
product permissions, so that changing provider never moves platform data. A user may belong to
several tenants, and tenants may later need different sign-in methods, including guest access.
Federation, account linking, and sign-in policies come from the provider where possible, so that
the platform does not build them.

## Requirements

### For the first integration

1. Sign browser users in so that the gateway can create its own session, through standard OIDC or
   a provider SDK that works with Spring Cloud Gateway on WebFlux.
2. Issue access tokens for the public API and MCP resources, never one per private service, so
   that the private service layout never reaches the provider or the consent screen.
3. Put the organization the user chose at sign-in or consent in each token, including tokens from
   MCP consent, so that the gateway can map the token to one tenant.
4. Support MCP authorization: authorization code with PKCE, resource indicators, and client
   registration through CIMD or DCR.
5. Show users which client is asking and what it may do, in public scope names, so that they know
   what they grant.
6. Cost little to run for a small lab, with predictable pricing as it grows.

### To design for now and build later

- Enterprise SAML or OIDC federation and social sign-in, possibly several per tenant.
- Linking sign-in identities to one platform principal, never by matching email alone, so that
  someone who controls a matching address elsewhere cannot take over an account.
- Guest users and tenant-specific sign-in requirements, including signing in again when switching
  tenant.

The [gateway ADR](gateway.md) sets one audience per product, named by its public URL, and when MCP
gets its own.

## Decision

The provider is not chosen yet. Auth0, WorkOS AuthKit, and self-hosted Keycloak are the
candidates. An earlier preference for Auth0 assumed one Auth0 API per private service, which these
requirements rule out.

Each candidate goes through the same proof of concept, listed under
[Resolve before accepting](#resolve-before-accepting), so that the choice rests on what each one
does rather than what its documentation says. The proof needs no custom account linking or tenant
policy engine.

## Candidates and questions to validate

- **Auth0.** Hosted OIDC, social and enterprise connections, organizations, APIs, consent, and MCP
  client registration. Check CIMD and DCR onboarding, resource indicators for MCP, WebFlux
  integration, and the price of enterprise connections and organizations. Admins may have to
  register CIMD clients, which could block arbitrary MCP clients.
- **WorkOS AuthKit.** Hosted sign-in, enterprise SSO, user management, and MCP support. Check the
  third-party consent screen and scope descriptions, account linking, session integration with
  Spring WebFlux, and the price of what is needed. Do not assume its Java SDK fits the gateway.
- **Keycloak.** Self-hosted, standards-based OIDC, federation, client scopes, and consent. Check how
  well CIMD, DCR, and resource indicators work with MCP clients. Running, securing, upgrading, and
  backing it up is a real cost.

Clerk, Zitadel, Ory, Cognito, Microsoft Entra External ID, FusionAuth, and Okta were considered
earlier. Revisit them only if no candidate meets the requirements, since some were ruled out by the
per-service audience assumption.

## Consequences

- Sign-in and consent come off the shelf. The gateway keeps sessions, public API security,
  routing, allowlisting, rate limiting, and internal identity tokens.
- The tenant service owns membership, product permissions, and tenant policy. Provider
  organizations are used without becoming the source of truth.
- Public scopes and audiences can change independently of private services.
- Provider SDKs and features add switching cost. Avoiding a home-grown identity system matters more
  than portability no one needs yet.
- This proposal chooses no provider and no session integration.

## Resolve before accepting

1. Sign a browser user in and create a gateway session in Spring Cloud Gateway.
2. Have an external MCP client complete authorization code with PKCE and consent, with the gateway
   validating the public audience and no API per private service.
3. Show the organization claim in tokens from browser sign-in and from MCP consent, mapped to a
   platform tenant.
4. Check registration friction, federation, account linking, and real pricing for a small lab and a
   multi-tenant product.
5. Record the chosen provider and its tradeoffs here.

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
