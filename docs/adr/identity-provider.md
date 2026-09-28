# Identity Provider

## Status

Proposed.

Date: 2026-09-26
Decision owner: Jack Vincent

## Context

The [edge gateway](gateway.md) is both the OAuth2 client for browser OIDC login and the OAuth2
resource server that validates bearer JWTs with a per-vertical audience. It needs an OpenID
Connect provider. A separate tenant service will own principals, tenants, and memberships, so the
gateway resolves platform identifiers by lookup. Having the provider mint platform claims is a
deferred alternative, so the provider must not block it.

Four criteria came out of the gateway discussion:

1. Custom audiences per vertical, with consent text per scope.
2. An organisation claim in issued tokens.
3. Self-registration for MCP clients.
4. Cost, for a lab with a handful of users and a credible path to production pricing.

Criterion 3 moved while this was being decided. The MCP authorization specification of
2026-07-28 says authorization servers SHOULD support
[Client ID Metadata Documents](https://modelcontextprotocol.io/specification/2026-07-28/basic/authorization)
and MAY support Dynamic Client Registration, which is deprecated and kept for backwards
compatibility. MCP clients MUST also send the RFC 8707 `resource` parameter and MCP servers MUST
check the token audience. A provider therefore needs CIMD or DCR, and ideally both, plus
`resource` handling.

## Decision

Use Auth0. It is the only candidate that meets all four criteria today with documented, generally
available tenant settings, and its free plan covers the lab.

### Audiences and consent

Each vertical is an Auth0 API with a URI identifier, which becomes the token audience. The
[Resource Parameter Compatibility Profile](https://auth0.com/ai/docs/mcp/guides/resource-param-compatibility-profile)
toggle makes the RFC 8707 `resource` parameter select that audience, and a second toggle adds the
`iss` parameter to authorization responses. Scope descriptions on the API become the consent text
when the `use_scope_descriptions_for_consent` tenant flag is set; first-party applications can
skip consent per API
([user consent](https://auth0.com/docs/get-started/applications/confidential-and-public-applications/user-consent-and-third-party-applications)).

### Organisation claim

Auth0 Organizations put `org_id` in access and ID tokens when the `organization` parameter is
passed, with `org_name` as an opt-in
([using tokens](https://auth0.com/docs/manage-users/organizations/using-tokens)). This keeps the
provider-minted claims alternative open. The lookup design does not need Organizations at all,
so the free plan's limit of five is not binding for the first iteration.

### MCP client registration

Auth0 supports
[Dynamic Client Registration](https://auth0.com/ai/docs/mcp/guides/registering-your-mcp-client-application/dynamic-client-registration)
behind a tenant toggle. Registered clients become third-party applications and need default
permissions set per API. CIMD is supported by
[manual registration](https://auth0.com/ai/docs/mcp/guides/registering-your-mcp-client-application/manual-cimd-registration):
an admin registers the document URL, which becomes the `client_id`, and refreshes metadata by
hand. Auth0 recommends CIMD over DCR for production. Automatic fetch of an unknown CIMD URL at
authorization time is not documented, so a new MCP client is either registered by an admin or
uses DCR.

### Cost

The [free plan](https://auth0.com/pricing) covers 25,000 monthly active users, one custom domain,
five Organizations, and 1,000 machine-to-machine tokens. Production pricing starts at $35 per
month for B2C Essentials and $150 per month for B2B Essentials, which lifts the Organizations
limit. Hardening options for an open DCR endpoint, such as tenant access control lists, are
Enterprise only.

### Risk

Auth0 could be wrong on cost. A tenant per Auth0 Organization is capped at five on the free plan
and needs the B2B plan beyond that, and DCR endpoint hardening needs Enterprise. If the platform
needs many tenants as Organizations before it has revenue, or must expose open DCR to the
internet, the bill jumps. The fallback is self-hosted Keycloak.

## Alternatives Considered

The finalists against each criterion:

| Criterion              | Auth0                            | Keycloak 26                                     | WorkOS AuthKit                           |
| ---------------------- | -------------------------------- | ----------------------------------------------- | ---------------------------------------- |
| Audience per vertical  | API identifier, `resource` param | Audience mapper per client scope, no `resource` | Resource indicators, `aud` = resource    |
| Consent text per scope | API scope descriptions           | Consent Screen Text per client scope            | Consent shown, per-scope text unverified |
| Organisation claim     | `org_id`, optional `org_name`    | `organization` claim, id optional               | `org_id`                                 |
| CIMD                   | Manual registration              | Experimental feature flag                       | Yes                                      |
| DCR                    | Yes                              | Yes                                             | Yes                                      |
| Cost for the lab       | Free to 25,000 MAU               | Free, self-hosted                               | Free to 1,000,000 MAU                    |

- **Keycloak, self-hosted.** Fallback. The
  [Audience mapper](https://www.keycloak.org/docs/latest/server_admin/index.html#audience-support)
  on an optional client scope gives an audience per vertical, and client scopes carry consent
  text. Organizations are fully supported since 26 and map to an `organization` claim. DCR is
  native. It falls short on MCP: CIMD is experimental behind `--features=cimd` and Keycloak does
  not recognise the `resource` parameter, so it only partially supports the 2026-07-28 spec
  ([MCP guide](https://www.keycloak.org/securing-apps/mcp-authz-server)). No licence cost, but
  it is another service with a database to run and patch.
- **WorkOS AuthKit.** Strongest on MCP and cost. CIMD and DCR are dashboard toggles, `resource`
  is honoured, tokens are JWTs with `org_id`
  ([MCP](https://workos.com/docs/authkit/mcp), [token claims](https://workos.com/docs/authkit/connect/token-claims)),
  and the first million monthly active users are free with organisations included
  ([pricing](https://workos.com/pricing)). Not chosen because per-scope consent text could not
  be confirmed from the documentation, a custom domain costs $99 per month, and there is no
  self-hosted option. Revisit if Auth0 pricing bites.
- **Clerk.** Close. CIMD, DCR, custom scopes, JWT access tokens, and `org_id` via
  `user:org:read` are all documented. Not chosen because per-resource audiences are not
  documented and the product is built around its front-end SDKs rather than a standalone
  provider.
- **Zitadel.** Open DCR exists specifically for MCP and organisation claims are a reserved
  scope, but there is no consent screen for third-party applications and CIMD is unreleased.
- **Ory.** Hydra has DCR but no CIMD. Ory Network organisations start on the Growth plan at
  roughly $9,350 per year, and the consent screen is an application you build yourself.
- **Amazon Cognito, Microsoft Entra External ID, FusionAuth.** No DCR and no CIMD, so MCP
  clients cannot self-register without a façade in front of the provider.
- **Okta.** Its customer identity product is Auth0. Workforce pricing does not fit a lab.

## Consequences

The gateway gets a hosted provider with OIDC login, JWKS, per-vertical audiences, and MCP client
registration without running identity infrastructure. Tenant, principal, and scope resolution stay
in the tenant service as decided in the gateway ADR.

New MCP clients need an admin to register their CIMD document or an open DCR endpoint. The DCR
endpoint cannot be network-restricted on the free plan, so it stays off until a client needs it.

Auth0 becomes a login-path dependency with a price step at production scale. The Keycloak fallback
keeps the protocol surface identical, so switching is a provider configuration change rather than
a gateway change.
