# Identity Gateway

This page summarizes the identity gateway prototype. The detailed decisions and tradeoffs live in
the [prototype ADR](../adr/identity-gateway-prototype.md).

## Goals

The prototype proves that we can:

1. log into Mops in a browser using a server-side session;
2. authorize an external application to call Mops APIs;
3. authenticate public API requests using bearer tokens;
4. authorize an external agent to use the remote Mops MCP server;
5. reuse the identity layer across Mops, Organizer, and future verticals.

The prototype ends when these five flows work. Tenant database lifecycle, profile projections,
membership webhooks, administration, and impersonation are separate future rounds.

## Architecture

```mermaid
flowchart LR
    Browser["Browser"]
    Client["External app or agent"]
    WorkOS["WorkOS<br/>Authentication and OAuth"]
    Gateway["Identity gateway<br/>Sessions, token validation, routing"]
    Redis["Redis<br/>Browser sessions"]
    IdentityDB["PostgreSQL<br/>Platform ID mappings"]
    MopsApp["Mops web app"]
    Mops["Mops service<br/>GraphQL and MCP"]
    Organizer["Organizer<br/>Future vertical"]

    Browser --> Gateway
    Client --> Gateway
    Gateway <--> WorkOS
    Gateway <--> Redis
    Gateway <--> IdentityDB
    Gateway --> MopsApp
    Gateway --> Mops
    Gateway -.-> Organizer
```

The gateway is the only public application entry point. WorkOS proves upstream identity and operates
the initial OAuth authorization server. The gateway maps provider identifiers to stable platform
identifiers and sends only provider-neutral context downstream.

## Ownership

| Concern                                        | Owner                              |
| ---------------------------------------------- | ---------------------------------- |
| Login and upstream authentication              | WorkOS adapter                     |
| OAuth consent and public token issuance        | WorkOS                             |
| Browser session lifecycle                      | Identity gateway and Redis         |
| Stable `principal_id` and `tenant_id` mappings | Identity gateway and PostgreSQL    |
| Public routing and token validation            | Identity gateway                   |
| Mops object and attribute authorization        | Mops                               |
| MCP tools and execution                        | Mops                               |
| Tenant database lifecycle                      | Deferred Mops/control-plane design |

WorkOS-specific IDs and claims stop at the gateway. Mops does not know which identity provider was
used.

## Browser Flow

```mermaid
sequenceDiagram
    participant B as Browser
    participant G as Identity gateway
    participant W as WorkOS
    participant R as Redis
    participant M as Mops

    B->>G: Open /mops/
    G->>W: Start login
    W-->>G: Authenticated callback
    G->>G: Map external user and organization
    G->>R: Create platform session
    G-->>B: Secure, HTTP-only session cookie
    B->>G: GraphQL request plus CSRF token
    G->>R: Resolve session
    G->>M: X-Principal-Id, X-Tenant-Id, X-Scopes
    M-->>G: Response
    G-->>B: Response
```

Browser JavaScript does not receive a bearer token for ordinary Mops requests. The platform session
is shared by verticals on the public origin, but each vertical independently decides what the
principal may do.

## External App And MCP Flow

```mermaid
sequenceDiagram
    participant C as External app or agent
    participant W as WorkOS
    participant G as Identity gateway
    participant M as Mops API or MCP

    C->>W: Authorization code flow with PKCE
    W-->>C: Access token
    C->>G: Bearer token
    G->>G: Validate issuer, audience, expiry, organization, scopes
    G->>G: Map to platform principal and tenant
    G->>M: Provider-neutral identity context
    M->>M: Enforce scopes and domain authorization
    M-->>C: Response through gateway
```

Each vertical has a distinct OAuth audience and owns the meaning of its scopes. Remote MCP uses the
same bearer-token boundary and the same Mops application authorization as GraphQL.

## Provider Portability

Only WorkOS is implemented in the prototype. Portability is proven through code boundaries:

- provider SDKs, claims, endpoints, and IDs remain inside adapters;
- platform sessions contain platform IDs rather than provider IDs;
- Mops receives the same identity headers regardless of provider;
- switching to Keycloak should require a new adapter, configuration, mapping migration, and OAuth
  client migration, not changes to Mops or session semantics.

The WorkOS capability spike must document actual consent, scope, resource, token-claim, and MCP
behavior before those assumptions become platform contracts.

## Delivery Roadmap

```mermaid
flowchart LR
    I1["1. WorkOS<br/>capability spike"]
    I1 --> I2["2. Shared identity<br/>contract"]
    I2 --> I3["3. Gateway and<br/>/mops/ routing"]
    I3 --> I4["4. Local Redis<br/>sessions"]
    I4 --> I5["5. WorkOS<br/>browser login"]
    I5 --> I6["6. External apps,<br/>bearer API, and MCP"]
```

Iteration 4 is the first local end-to-end browser milestone. Iteration 6 completes the prototype.

## Detailed Records

- [Identity gateway prototype ADR](../adr/identity-gateway-prototype.md)
- [Draft: Identity operations](../adr/identity-operations-draft.md)
- [Draft: Mops tenant data lifecycle](../adr/mops-tenant-data-lifecycle-draft.md)
