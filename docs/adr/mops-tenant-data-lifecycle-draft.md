# Draft: Mops Tenant Data Lifecycle

## Status

Draft for a future delivery round.

Date: 2026-06-10
Decision owner: Jack Vincent

This record captures future tenant-data lifecycle direction. It is not required to prove the
identity gateway prototype and must be reviewed again before implementation.

See the [identity gateway overview](docs/platform/identity-gateway.md) for the committed prototype boundary.

## Context

Mops is expected to serve hundreds of tenants. Desired lifecycle operations include deleting all
tenant data, exporting or moving a tenant between environments, and cloning production data into a
sandbox tenant in the same environment.

The platform owns the canonical opaque `tenant_id`. Mops must not create a second canonical tenant
identity.

The expected production ownership model is a control plane that owns canonical tenant lifecycle and
product membership. It provisions or links upstream WorkOS organizations, then drives idempotent
vertical provisioning. The identity gateway keeps the local projection required for sessions and
tokens; WorkOS may become a synchronized authentication and OAuth projection rather than the
canonical membership source. Request handling must not call the control plane synchronously.

## Historical Context

Mops previously generated tenant IDs through its own in-memory tenant model and public provisioning
mutation. That model was removed because canonical tenant identity belongs to the platform. A future
Mops tenant representation is an application provisioning record and database keyed by the platform
`tenant_id`, not a separate identity aggregate.

## Database-Per-Tenant Direction

Use one PostgreSQL database per tenant. For hundreds of tenants this provides a useful isolation and
lifecycle boundary:

- Delete tenant data by dropping its database.
- Export, restore, or move a tenant independently.
- Clone a tenant database into a separately identified sandbox tenant.
- Reduce the risk of missing tenant predicates in application queries.
- Permit future tenant-specific backup, residency, or maintenance policies.

The costs are fleet-wide migrations, connection-pool management, provisioning automation, and a
separate approach for cross-tenant analytics.

## Registry And Runtime Routing

A small central Mops registry should map canonical `tenant_id` values to database location and
status. The registry and gateway identity data use separate logical PostgreSQL databases, though
they may share one cluster.

Tenant databases initially share one PostgreSQL cluster and one Mops database role. Runtime
datasources are created on first use, cached with small per-tenant pools, bounded globally, and
closed after an idle period. Startup migrations use short-lived connections rather than opening all
runtime pools.

## Migrations

For the first database-per-tenant implementation, Mops enumerates every provisioned tenant database
and runs Flyway during startup. Startup fails and readiness remains false if any provisioned database
cannot migrate.

The service code and migration resources ship in the same OCI image. Migrations must remain
compatible with a rolling deployment while old pods may still serve traffic. A dedicated release
migration job can replace startup migration when replica count, migration duration, permissions, or
operational visibility makes it necessary.

## Provisioning

Mops should expose an idempotent private operation such as:

```http
PUT /internal/tenants/{tenant_id}
```

For the initial implementation it may synchronously create the database, run migrations, register
routing metadata, and return only when the tenant is ready. Concurrent calls for one tenant must be
serialized. A failed record remains diagnosable and retryable.

The private endpoint uses one static bearer token from environment or Kubernetes secrets. A future
workload identity or mTLS mechanism can replace it.

A minimal control-plane CLI may orchestrate WorkOS organization setup, platform mapping creation,
and the Mops provisioning call. A future control-plane service and admin UI can reuse the same
vertical provisioning contract.

Provisioning occurs as part of tenant onboarding, before users receive access. It is not normally
triggered by first application use. A warm database pool or template cloning may later reduce
latency if provisioning time proves material.

## Defensive Unprovisioned State

An authenticated tenant without a ready Mops registry entry is an application-provisioning state,
not an authentication failure. Mops should return a structured `APPLICATION_NOT_PROVISIONED` result
and the frontend should render an unavailable or setup state.

## Deferred Operations

- Tenant database clone and sandbox workflows.
- Database export, restore, and environment movement.
- Tenant deletion and retention controls.
- Cross-tenant analytics or event export.
- Tenant cohorts, quarantining, or partial deployment gates.
- Per-tenant database roles or dedicated clusters.

## Open Questions

- Which component ultimately owns the central Mops registry?
- What database naming and credential model fits the production PostgreSQL platform?
- What migration duration and availability targets trigger a dedicated migration job?
- How are destructive clone, restore, and delete operations authorized and audited?
