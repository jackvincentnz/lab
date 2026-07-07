# Draft: Identity Operations

## Status

Draft for a future delivery round.

Date: 2026-06-10
Decision owner: Jack Vincent

This record captures future identity-operations direction. It is not part of the committed identity
gateway prototype and should be revisited after the prototype exit criteria are met.

See the [identity gateway overview](../platform/identity-gateway.md) for the committed prototype boundary.

## Context

The identity gateway prototype establishes browser sessions, external application authorization,
bearer-token authentication, remote MCP authorization, and a provider-neutral identity contract.
Production operation will eventually need richer profile rendering, revocation, auditing,
administration, and support workflows.

## Profile Projection

The identity layer may cache a minimal, non-canonical profile from the configured identity provider.
Mops may keep a local `actor_profile` projection containing:

```text
principal_id
display_name
avatar_url
profile_version
updated_at
```

The gateway may carry bounded trusted profile fields on authenticated requests so Mops can update
the projection opportunistically without adding an event stream or synchronous profile dependency.
Profile fields are never used for authorization.

Ordinary and audit views resolve the current projected display name using the recorded
`principal_id`. Audit records do not snapshot profile fields. Email is omitted from the initial Mops
projection. A future profile page belongs to the platform identity surface rather than Mops.

## Request Correlation And Access Logs

The gateway should emit structured access logs containing:

```text
request_id
principal_id
tenant_id
actor_principal_id
credential_type
route
method
status
latency
source_ip
user_agent
```

The gateway generates or normalizes one request ID, includes it in access logs, forwards it to
downstream services, and returns it to clients. Downstream domain logs use the same request ID.

Cookies, bearer tokens, authorization codes, and sensitive request bodies must not be logged.

## Membership Revocation

WorkOS membership webhooks may invalidate matching Redis sessions promptly. The prototype-level
implementation can verify signatures, process relevant events synchronously, rely on WorkOS retries,
and log provider event IDs without adding an inbox table or message broker.

Browser requests trust membership captured in the first-party session until expiry or webhook
revocation. Bearer-token requests trust verified organization claims until token expiry. Higher-risk
operations may later use token introspection or a local revocation cache.

Active WebSockets should close when their associated session expires or is invalidated by logout or
membership revocation.

## Platform Administration

Platform administration is separate from tenant membership. A special "admin tenant" must not be
used as the authority for cross-tenant operations. An administrator may independently hold ordinary
tenant memberships.

## Impersonation

Impersonation should create a short-lived derived session rather than modify the administrator's
normal session. The derived session preserves:

```text
actor_principal_id
effective_principal_id
effective_tenant_id
reason
started_at
expires_at
```

Downstream services authorize the effective principal while audit context retains the real actor.
Impersonation requires renewed authentication, a reason, strict expiry, prominent UI, and an
immediate exit operation. It must not expose or reuse the target user's credentials.

## Open Questions

- Which profile fields are required by the first real activity or audit view?
- What revocation latency is acceptable for browser and bearer credentials?
- Which platform permissions gate administration and impersonation?
- Which audit events require immutable storage beyond ordinary application logs?
