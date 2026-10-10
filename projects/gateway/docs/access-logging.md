# Access logging

The gateway writes one structured record for each request. The console log is JSON in the
Logstash format, one object per line, so the record's fields are flat top-level keys next to
`@timestamp`, `level`, and `message`:

```json
{
  "@timestamp": "2026-10-10T06:27:11.041653Z",
  "message": "Access",
  "logger_name": "lab.gateway.AccessLogHandler",
  "method": "GET",
  "path": "/api/graphql",
  "status": 200,
  "duration_ms": 50,
  "source_ip": "127.0.0.1",
  "request_id": "cd790ad7-d6a4-4d0a-b077-4c7cba4aaee3",
  "tenant_id": "22222222-2222-2222-2222-222222222222",
  "principal_id": "11111111-1111-1111-1111-111111111111",
  "authentication_method": "form"
}
```

| Field                   | Source                                                                    |
| ----------------------- | ------------------------------------------------------------------------- |
| `method`                | The request's HTTP method.                                                |
| `path`                  | The public path, before routes rewrite it. The query string is omitted.   |
| `status`                | The status the gateway returned, including ones the security chain set.   |
| `duration_ms`           | Time from the request arriving to the response completing.                |
| `source_ip`             | The socket peer. Forwarded headers such as `X-Forwarded-For` are ignored. |
| `request_id`            | A UUID the gateway mints for this request.                                |
| `tenant_id`             | The authenticated caller's active tenant.                                 |
| `principal_id`          | The authenticated caller's principal.                                     |
| `authentication_method` | How the caller authenticated, as the identity token's `amr` value.        |

A field the gateway does not know for a request is left out rather than written empty. The record
is built from named fields only, so header values, cookies, tokens, and request bodies such as the
login form never reach it.

## How the record is built

Spring Boot observes every request as `http.server.requests`. That observation is the request's
context: each part of the gateway that learns something about the request adds it to the
observation, and `AccessLogHandler` writes the record when the observation stops, after the
response is complete.

- `RequestIdWebFilter` runs first and adds `request_id`.
- A filter in the security chain, after authentication, adds the caller fields from the security
  context. It runs before logout, so a logout record names the caller who logged out.
- `AccessLogHandler` reads `method`, `path`, `status`, `duration_ms`, and `source_ip` from the
  request and response.

Added fields are high-cardinality key values, so they never become tags on the
`http.server.requests` metric. A new field is added the same way: add it to the observation where
the gateway learns it, and name it in `AccessLogHandler`.

## Request ID

The gateway mints a request ID for every request, with or without a session, and replaces any
`X-Request-ID` the client sent so a client cannot choose the ID that logs are correlated on. The
ID is forwarded to the downstream service as `X-Request-ID` and returned to the client in the
`X-Request-ID` response header.

## Requests without a caller

Requests without an authenticated caller are recorded too: the login page, the login form
submission, health, the JWK set, and requests the gateway answers with 401 or a redirect to login.
Their records leave out `tenant_id`, `principal_id`, and `authentication_method`.

The caller fields are added after authentication, so a request the security chain ends before
that point is recorded without them even when it carries a session. A request rejected for a
missing CSRF token is one: it is recorded with its path and the 403 status only. A successful
login is recorded without a caller, and a logout with the caller who logged out.

A request whose client disconnects before the response completes is recorded without `status`.
