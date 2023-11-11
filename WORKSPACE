# Declares that this directory is the root of a Bazel workspace.
# See https://docs.bazel.build/versions/main/build-ref.html#workspace
workspace(
    # How this workspace would be referenced with absolute labels from another workspace
    name = "lab",
)

load("//tools/bazel:bazel_deps.bzl", "fetch_dependencies")

fetch_dependencies()

####################################################################################################
# rules_proto setup
####################################################################################################

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

####################################################################################################
# maven setup
####################################################################################################

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

# Check the spring dependency management to see versions which work together:
# e.g. https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-dependencies/3.0.12
SPRING_BOOT_DEPENDENCIES = [
    "org.apache.commons:commons-lang3:3.12.0",
    "org.apache.kafka:kafka-clients:3.3.2",
    "org.apache.kafka:kafka-metadata:3.3.2",
    "org.apache.kafka:kafka-raft:3.3.2",
    "org.apache.kafka:kafka-server-common:3.3.2",
    "org.apache.kafka:kafka-storage:3.3.2",
    "org.apache.kafka:kafka-storage-api:3.3.2",
    "org.apache.kafka:kafka-streams:3.3.2",
    "org.apache.kafka:kafka-streams-test-utils:3.3.2",
    "org.apache.kafka:kafka_2.13:3.3.2",
    "org.springframework.boot:spring-boot-loader:3.0.12",
    "org.springframework.boot:spring-boot-starter-validation:3.0.12",
    "org.springframework.boot:spring-boot-starter-web:3.0.12",
    "org.springframework.boot:spring-boot-starter-webflux:3.0.12",
    "org.springframework.kafka:spring-kafka:3.0.12",
    maven.artifact(
        "org.springframework.boot",
        "spring-boot-starter-test",
        "3.0.12",
        testonly = True,
    ),
    maven.artifact(
        "org.springframework.kafka",
        "spring-kafka-test",
        "3.0.12",
        testonly = True,
    ),
    maven.artifact(
        "org.testcontainers",
        "kafka",
        "1.18.3",
        testonly = True,
    ),
    maven.artifact(
        "org.junit.platform",
        "junit-platform-launcher",
        "1.9.3",
        testonly = True,
    ),
    maven.artifact(
        "org.junit.platform",
        "junit-platform-reporting",
        "1.9.3",
        testonly = True,
    ),
]

# Maven lock file will need to be updated whenever the artifacts or repositories change.
# See: https://github.com/bazelbuild/rules_jvm_external#requiring-lock-file-repinning-when-the-list-of-artifacts-changes
# To re-pin everything, run:
# REPIN=1 bazel run @unpinned_maven//:pin
maven_install(
    artifacts = SPRING_BOOT_DEPENDENCIES + [
        "com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:7.5.1",
        maven.artifact(
            artifact = "graphql-dgs-codegen-core",
            exclusions = [
                "com.graphql-java:graphql-java",
            ],
            group = "com.netflix.graphql.dgs.codegen",
            version = "6.0.2",
        ),
        "io.confluent:kafka-protobuf-serializer:7.5.1",
    ],
    fail_if_repin_required = True,
    fetch_sources = True,
    maven_install_json = "//:maven_install.json",
    repositories = [
        "https://repo1.maven.org/maven2",
        "https://packages.confluent.io/maven",
    ],
    version_conflict_policy = "pinned",
)

load("@maven//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

####################################################################################################
# contrib_rules_jvm setup
####################################################################################################

# Fetches the contrib_rules_jvm dependencies.
# If you want to have a different version of some dependency,
# you should fetch it *before* calling this.
load("@contrib_rules_jvm//:repositories.bzl", "contrib_rules_jvm_deps")

contrib_rules_jvm_deps()

# Now ensure that the downloaded deps are properly configured
load("@contrib_rules_jvm//:setup.bzl", "contrib_rules_jvm_setup")

contrib_rules_jvm_setup()

####################################################################################################
# aspect_rules_ts setup
####################################################################################################

load("@aspect_rules_ts//ts:repositories.bzl", "rules_ts_dependencies")

rules_ts_dependencies(
    ts_version_from = "//:package.json",
)

####################################################################################################
# rules_nodejs setup
####################################################################################################

load("@rules_nodejs//nodejs:repositories.bzl", "DEFAULT_NODE_VERSION", "nodejs_register_toolchains")

nodejs_register_toolchains(
    name = "nodejs",
    node_version = DEFAULT_NODE_VERSION,
)

####################################################################################################
# aspect_rules_js setup
####################################################################################################

load("@aspect_rules_js//npm:npm_import.bzl", "npm_translate_lock")

npm_translate_lock(
    name = "npm",
    bins = {
        # derived from "bin" attribute in node_modules/react-scripts/package.json
        "react-scripts": {
            "react-scripts": "./bin/react-scripts.js",
        },
    },
    npmrc = "//:.npmrc",
    pnpm_lock = "//:pnpm-lock.yaml",
    verify_node_modules_ignored = "//:.bazelignore",
)

load("@npm//:repositories.bzl", "npm_repositories")

npm_repositories()

####################################################################################################
# aspect_rules_jest setup
####################################################################################################

load("@aspect_rules_jest//jest:dependencies.bzl", "rules_jest_dependencies")

rules_jest_dependencies()

####################################################################################################
# bazel_gazelle setup
####################################################################################################

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")

go_rules_dependencies()

go_register_toolchains(version = "1.19.5")

load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")

gazelle_dependencies()

####################################################################################################
# rules_oci setup
####################################################################################################

load("@rules_oci//oci:dependencies.bzl", "rules_oci_dependencies")

rules_oci_dependencies()

load("@rules_oci//oci:repositories.bzl", "LATEST_CRANE_VERSION", "oci_register_toolchains")

oci_register_toolchains(
    name = "oci",
    crane_version = LATEST_CRANE_VERSION,
    # Uncommenting the zot toolchain will cause it to be used instead of crane for some tasks.
    # Note that it does not support docker-format images.
    # zot_version = LATEST_ZOT_VERSION,
)

load("@rules_oci//oci:pull.bzl", "oci_pull")

# A single-arch base image
oci_pull(
    name = "distroless_java",
    digest = "sha256:052076466984fd56979c15a9c3b7433262b0ad9aae55bc0c53d1da8ffdd829c3",
    image = "gcr.io/distroless/java17",
)

oci_pull(
    name = "nginx_debian_slim",
    digest = "sha256:48a84a0728cab8ac558f48796f901f6d31d287101bc8b317683678125e0d2d35",
    image = "docker.io/library/nginx",
)

####################################################################################################
# rules_pkg setup
####################################################################################################

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

####################################################################################################
# aspect_rules_swc setup
####################################################################################################

load("@aspect_rules_swc//swc:dependencies.bzl", "rules_swc_dependencies")

rules_swc_dependencies()

load("@aspect_rules_swc//swc:repositories.bzl", "LATEST_SWC_VERSION", "swc_register_toolchains")

swc_register_toolchains(
    name = "swc",
    swc_version = LATEST_SWC_VERSION,
)
