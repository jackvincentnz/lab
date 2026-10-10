# Access logging

The gateway writes one structured record for each request. The console log is JSON in the Logstash format, one object per line, so the record's fields are
flat top-level keys next to `@timestamp`, `level`, and `message`:

```json
{
  "@timestamp": "2026-10-09T23:52:17.822835Z",
  "message": "Access",
  "logger_name": "lab.gateway.AccessLogWebFilter",
  "source_ip": "127.0.0.1",
  "tenant_id": "22222222-2222-2222-2222-222222222222",
  "principal_id": "11111111-1111-1111-1111-111111111111",
  "authentication_method": "form",
  "path": "/api/graphql",
  "status": 200,
  "request_id": "f2fe56d3-b106-4bc8-acb6-eed536fae263"
}
```

| Field                   | Source                                                                                  |
| ----------------------- | --------------------------------------------------------------------------------------- |
| `source_ip`             | The socket peer. Forwarded headers such as `X-Forwarded-For` are ignored.               |
| `tenant_id`             | The session's active tenant. Only with a session.                                       |
| `principal_id`          | The session's principal. Only with a session.                                           |
| `authentication_method` | How the caller authenticated, as the identity token's `amr` value. Only with a session. |
| `path`                  | The public path, before routes rewrite it. The query string is omitted.                 |
| `status`                | The status the gateway returned, including ones the security chain set.                 |
| `request_id`            | A UUID the gateway mints for this request.                                              |

The record is built from named fields only, so header values, cookies, tokens, and request
bodies such as the login form never reach it.

## Request ID

The gateway mints a request ID for every request, with or without a session, and replaces any
`X-Request-ID` the client sent so a client cannot choose the ID that logs are correlated on. The
ID is forwarded to the downstream service as `X-Request-ID` and returned to the client in the
`X-Request-ID` response header.

## Requests without a session

Requests that arrive without a session are recorded too: the login page, the login form
submission, health, the JWK set, and requests the gateway answers with 401 or a redirect to login.
Their records leave out `tenant_id`, `principal_id`, and `authentication_method` rather than
writing them empty. The caller is the one the request arrived with, so a successful login is
recorded without a caller and a logout with one. A request with a session that the gateway
rejects, such as one missing its CSRF token, is recorded with the caller and the status it was
given.
