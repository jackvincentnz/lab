# Protobuf Upgrades

Keep these versions aligned:

- Bazel module/protoc: `protobuf@X.Y` in `MODULE.bazel`.
- Java runtime: `com.google.protobuf:protobuf-bom:4.X.Y`.
- Runtime artifacts should resolve to matching `protobuf-java` and `protobuf-java-util` versions in `maven_install.json`.

Upgrade flow:

```zsh
bazel mod graph --extension_info=usages
REPIN=1 bazel run @maven//:pin
bazel test //learn/kafka/...
bazel test //projects/organizer/... //projects/mops/service/...
bazel test //...
```

Watch for:

- Bzlmod direct dependency warnings; align root `bazel_dep` pins when needed.
- Confluent protobuf serializer compatibility; Kafka/protobuf tests are the signal.
- `AGENTS.md` protobuf invariant; update it after the upgrade.
