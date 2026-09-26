# Edge gateway

Run the empty WebFlux gateway:

```sh
bazel run //projects/gateway
curl --fail http://localhost:8080/actuator/health
```

The public health endpoint returns HTTP 200 with `"status":"UP"`. To use another port, run
`bazel run //projects/gateway -- --server.port=8081`.

Run the startup and HTTP health check test:

```sh
bazel test //projects/gateway/...
```

Spring Cloud is pinned through its BOM in `MODULE.bazel`. Spring Session's Redis
module is also pinned, ready for the session implementation; this empty gateway
does not yet configure sessions or require Redis. Routing and authentication are
subsequent steps in [the gateway tracker](https://github.com/jackvincentnz/lab/issues/962).
