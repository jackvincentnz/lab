---
status: Proposed
date: 2026-10-17
owner: Jack Vincent
---

# Identity Provider

## Context

The [edge gateway](gateway.md) is the platform's only OAuth2 client and resource server. It logs
browsers in with form login and Redis sessions, validates bearer JWTs against an issuer's JWK set
configured through Spring Boot's resource-server properties, and mints its own identity token for
downstream services. Replacing form login needs an OpenID Connect provider that serves:

- Browser login through the authorization code flow with Spring Security `oauth2Login`.
- Access tokens for API clients, with an audience per service.

The gateway design named four criteria:

1. An audience per service, with consent text per scope.
2. An organisation claim in issued tokens.
3. Registration for MCP clients.
4. No cost for a lab with a handful of users, and a known price beyond that.

The MCP authorization specification of 2026-07-28 says authorization servers SHOULD support
Client ID Metadata Documents (CIMD) and MAY support Dynamic Client Registration (DCR), which it
deprecates. MCP clients MUST send the RFC 8707 `resource` parameter, and MCP servers MUST reject
tokens issued for another audience. Criterion 3 therefore means CIMD, with DCR as a fallback, and
criterion 1 includes honouring `resource`.

## Decision

Use Auth0 as the identity provider, with self-hosted Keycloak as the fallback. Auth0 meets all four
criteria with documented tenant settings and no infrastructure to run, and its free plan covers the
lab. The cost it accepts is a hosted login dependency whose price steps up when tenants become
Auth0 Organizations.

- **Audience and consent.** Each service is an Auth0 API whose identifier is the token `aud`. The
  Resource Parameter Compatibility Profile setting makes `resource` select that API. Scope
  descriptions on the API become consent text, and first-party applications such as the gateway
  can skip consent.
- **Organisation claim.** Auth0 Organizations add `org_id` to ID and access tokens when login
  names an organization, and `org_name` when enabled.
- **MCP clients.** An admin imports a client's CIMD URL, which becomes its `client_id`. Auth0 does
  not fetch an unregistered URL at authorization time. DCR is a tenant setting that stays off,
  because only Enterprise plans can restrict who calls the registration endpoint.
- **Cost.** The free plan includes 25,000 monthly active users, five Organizations, five Actions,
  and one custom domain. B2C Essentials starts at $35 per month with 10 Organizations, and B2B
  Essentials at $150 per month with unlimited Organizations.

The gateway integrates through standard settings. `oauth2Login` uses Auth0's OIDC discovery with
the gateway as a first-party confidential application. Bearer validation keeps the resource-server
properties, set to the Auth0 issuer and an API identifier.

Auth0's `sub` is its own user ID, such as `auth0|…`, and Actions cannot change it. The gateway
therefore maps `sub` to a principal, which matches the gateway design's default of a lookup against
the tenant service. A post-login Action can add `tenant` and `scope` claims if the platform instead
chooses provider-minted claims.

This record leaves open:

- Lookup versus provider-minted claims. Auth0 supports both.
- Whether each tenant is an Auth0 Organization. Lookup does not need it, provider-minted tenant
  claims or per-tenant login do, and it decides the plan.
- API identifiers. They should be each service's public URI, which waits on the public URL
  structure.

## Consequences

- The gateway gets OIDC login, a JWK set, per-service audiences, consent, and MCP client
  registration without running identity infrastructure.
- Auth0 is on the path of every browser login and every new bearer token.
- Bearer validation must accept Auth0's `sub` and resolve principal and tenant from it, rather than
  require platform identifiers in the issuer's token.
- Each new MCP client needs an admin to import its CIMD URL.
- More than five tenants as Organizations costs $150 per month. Confidential CIMD clients use
  private key JWT authentication, and open DCR needs tenant access control lists; both are
  Enterprise only.
- Moving to Keycloak changes provider configuration and the `sub` mapping, not the gateway's
  protocols.

## Alternatives considered

- **Keycloak, self-hosted.** The fallback. Audience mappers give an audience per service, client
  scopes carry consent text, Organizations add an `organization` claim, and DCR is built in. CIMD
  and `resource` handling are experimental features, and its MCP guide lists the 2026-07-28
  specification as experimental. No licence cost, but another service and database to run, patch,
  and back up.
- **WorkOS AuthKit.** Strongest on MCP and cost: CIMD and DCR are settings, `resource` sets `aud`,
  tokens carry `org_id`, and the first million monthly active users are free. Not chosen because
  per-scope consent text is not documented, a custom domain costs $99 per month, and it cannot be
  self-hosted.
- **Clerk.** Supports CIMD, DCR, and an organization claim, but does not document an audience per
  resource, and is built around its front-end SDKs rather than as a standalone provider.
- **Zitadel and Ory.** Zitadel has no consent screen for third-party applications. Ory leaves the
  consent screen to the application and charges for organizations on paid plans.
- **Amazon Cognito, Microsoft Entra External ID, and FusionAuth.** No DCR or CIMD, so MCP clients
  need a registration façade in front of the provider.
- **Okta.** Its customer identity product is Auth0, and workforce pricing does not fit a lab.

## Reconsider when

- Tenants must be Auth0 Organizations beyond the free plan before the platform earns revenue:
  compare B2B Essentials with WorkOS and self-hosted Keycloak.
- MCP clients must register without an admin step: compare Auth0 Enterprise with WorkOS.
- Keycloak's CIMD and resource indicator support become fully supported: compare it as the primary
  provider.
- WorkOS documents per-scope consent text: compare it as the primary provider.

## References

- [MCP authorization, 2026-07-28](https://modelcontextprotocol.io/specification/2026-07-28/basic/authorization).
- [Auth0 pricing](https://auth0.com/pricing).
- [Auth0 Resource Parameter Compatibility Profile](https://auth0.com/ai/docs/mcp/guides/resource-param-compatibility-profile).
- [Auth0 user consent](https://auth0.com/docs/get-started/applications/confidential-and-public-applications/user-consent-and-third-party-applications).
- [Auth0 Organizations in tokens](https://auth0.com/docs/manage-users/organizations/using-tokens).
- [Auth0 custom claims](https://auth0.com/docs/secure/tokens/json-web-tokens/create-custom-claims).
- [Auth0 CIMD registration](https://auth0.com/docs/get-started/auth0-overview/create-applications/register-applications-with-cimd).
- [Auth0 DCR for MCP](https://auth0.com/ai/docs/mcp/guides/registering-your-mcp-client-application/dynamic-client-registration).
- [Keycloak as an MCP authorization server](https://www.keycloak.org/securing-apps/mcp-authz-server).
- [WorkOS AuthKit MCP](https://workos.com/docs/authkit/mcp) and [pricing](https://workos.com/pricing).
- [Spring Security OAuth2 login](https://docs.spring.io/spring-security/reference/reactive/oauth2/login/index.html).
