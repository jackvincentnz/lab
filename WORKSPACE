# Declares that this directory is the root of a Bazel workspace.
# See https://docs.bazel.build/versions/main/build-ref.html#workspace
workspace(
    # How this workspace would be referenced with absolute labels from another workspace
    name = "lab",
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

####################################################################################################
# rules_proto setup
####################################################################################################

http_archive(
    name = "rules_proto",
    sha256 = "dc3fb206a2cb3441b485eb1e423165b231235a1ea9b031b4433cf7bc1fa460dd",
    strip_prefix = "rules_proto-5.3.0-21.7",
    urls = [
        "https://github.com/bazelbuild/rules_proto/archive/refs/tags/5.3.0-21.7.tar.gz",
    ],
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

####################################################################################################
# maven setup
####################################################################################################

RULES_JVM_EXTERNAL_TAG = "5.3"

RULES_JVM_EXTERNAL_SHA = "d31e369b854322ca5098ea12c69d7175ded971435e55c18dd9dd5f29cc5249ac"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/%s/rules_jvm_external-%s.tar.gz" % (RULES_JVM_EXTERNAL_TAG, RULES_JVM_EXTERNAL_TAG),
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

# Check the spring dependency management to see versions which work together:
# e.g. https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-dependencies/3.0.12
SPRING_BOOT_DEPENDENCIES = [
    "com.fasterxml.jackson.core:jackson-databind:2.14.3",
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
    "org.eclipse.jetty:jetty-reactive-httpclient:3.0.10",
    "org.springframework.boot:spring-boot-loader:3.0.12",
    "org.springframework.boot:spring-boot-starter-actuator:3.0.12",
    "org.springframework.boot:spring-boot-starter-validation:3.0.12",
    "org.springframework.boot:spring-boot-starter-web:3.0.12",
    "org.springframework.boot:spring-boot-starter-webflux:3.0.12",
    "org.springframework.kafka:spring-kafka:3.0.12",
    "redis.clients:jedis:5.0.0",
    "org.postgresql:postgresql:42.7.0",
    "com.eventstore:db-client-java:5.0.0",
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
        "testcontainers",
        "1.18.3",
        #testonly = True,
    ),
    maven.artifact(
        "org.testcontainers",
        "junit-jupiter",
        "1.18.3",
        testonly = True,
    ),
    maven.artifact(
        "org.testcontainers",
        "kafka",
        "1.18.3",
        testonly = True,
    ),
    maven.artifact(
        "org.testcontainers",
        "postgresql",
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
        maven.artifact(
            "org.wiremock",
            "wiremock",
            "3.3.1",
            testonly = True,
        ),
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

http_archive(
    name = "contrib_rules_jvm",
    sha256 = "922499b167c946b2c2e5e4eacef0fb67ce349ddaa2301dfaa7bc97b39f476786",
    strip_prefix = "rules_jvm-0.21.0",
    url = "https://github.com/bazel-contrib/rules_jvm/releases/download/v0.21.0/rules_jvm-v0.21.0.tar.gz",
)

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

http_archive(
    name = "rules_spring",
    sha256 = "01426d0a67c32ba0de0b0f3baa2b0810087789c6260c0c06741c1733956158a3",
    urls = [
        "https://github.com/salesforce/rules_spring/releases/download/2.2.4/rules-spring-2.2.4.zip",
    ],
)

####################################################################################################
# rules_nodejs setup
####################################################################################################

http_archive(
    name = "rules_nodejs",
    sha256 = "f36e4a4747210331767033dc30728ae3df0856e88ecfdc48a0077ba874db16c3",
    strip_prefix = "rules_nodejs-6.0.3",
    url = "https://github.com/bazelbuild/rules_nodejs/releases/download/v6.0.3/rules_nodejs-v6.0.3.tar.gz",
)

load("@rules_nodejs//nodejs:repositories.bzl", "nodejs_register_toolchains")

nodejs_register_toolchains(
    name = "nodejs",
    node_version = "18.18.0",  # keep in sync with package.json
)

####################################################################################################
# aspect_rules_ts setup
####################################################################################################

http_archive(
    name = "aspect_rules_ts",
    sha256 = "8aabb2055629a7becae2e77ae828950d3581d7fc3602fe0276e6e039b65092cb",
    strip_prefix = "rules_ts-2.0.0",
    url = "https://github.com/aspect-build/rules_ts/releases/download/v2.0.0/rules_ts-v2.0.0.tar.gz",
)

load("@aspect_rules_ts//ts:repositories.bzl", "rules_ts_dependencies")

rules_ts_dependencies(
    ts_version_from = "//:package.json",
)

####################################################################################################
# aspect_rules_js setup
####################################################################################################

load("@aspect_rules_js//js:repositories.bzl", "rules_js_dependencies")

rules_js_dependencies()

load("@bazel_features//:deps.bzl", "bazel_features_deps")

bazel_features_deps()

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
    public_hoist_packages = {
        "@vitest/expect": [""],
        "@jest/expect": [""],
    },
    verify_node_modules_ignored = "//:.bazelignore",
)

load("@npm//:repositories.bzl", "npm_repositories")

npm_repositories()

####################################################################################################
# aspect_rules_jest setup
####################################################################################################

http_archive(
    name = "aspect_rules_jest",
    sha256 = "a2f35eedce1a59f94b2158b056caab09ed66b92cafabd631b25dac78a178e044",
    strip_prefix = "rules_jest-0.17.0",
    url = "https://github.com/aspect-build/rules_jest/releases/download/v0.17.0/rules_jest-v0.17.0.tar.gz",
)

load("@aspect_rules_jest//jest:dependencies.bzl", "rules_jest_dependencies")

rules_jest_dependencies()

####################################################################################################
# bazel_gazelle setup
####################################################################################################

http_archive(
    name = "io_bazel_rules_go",
    sha256 = "278b7ff5a826f3dc10f04feaf0b70d48b68748ccd512d7f98bf442077f043fe3",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_go/releases/download/v0.41.0/rules_go-v0.41.0.zip",
        "https://github.com/bazelbuild/rules_go/releases/download/v0.41.0/rules_go-v0.41.0.zip",
    ],
)

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")

go_rules_dependencies()

go_register_toolchains(version = "1.19.5")

http_archive(
    name = "bazel_gazelle",
    sha256 = "b7387f72efb59f876e4daae42f1d3912d0d45563eac7cb23d1de0b094ab588cf",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-gazelle/releases/download/v0.34.0/bazel-gazelle-v0.34.0.tar.gz",
        "https://github.com/bazelbuild/bazel-gazelle/releases/download/v0.34.0/bazel-gazelle-v0.34.0.tar.gz",
    ],
)

load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")

gazelle_dependencies()

####################################################################################################
# rules_oci setup
####################################################################################################

http_archive(
    name = "rules_oci",
    sha256 = "21a7d14f6ddfcb8ca7c5fc9ffa667c937ce4622c7d2b3e17aea1ffbc90c96bed",
    strip_prefix = "rules_oci-1.4.0",
    url = "https://github.com/bazel-contrib/rules_oci/releases/download/v1.4.0/rules_oci-v1.4.0.tar.gz",
)

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
    digest = "sha256:ce7b2c695953fba6579d3d524590d7a7e67236d814fbd6f6769fe10e800e758d",
    image = "gcr.io/distroless/java17",
)

oci_pull(
    name = "nginx_debian_slim",
    digest = "sha256:9784f7985f6fba493ba30fb68419f50484fee8faaf677216cb95826f8491d2e9",
    image = "docker.io/library/nginx",
)

####################################################################################################
# rules_pkg setup
####################################################################################################

http_archive(
    name = "rules_pkg",
    sha256 = "8f9ee2dc10c1ae514ee599a8b42ed99fa262b757058f65ad3c384289ff70c4b8",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/releases/download/0.9.1/rules_pkg-0.9.1.tar.gz",
        "https://github.com/bazelbuild/rules_pkg/releases/download/0.9.1/rules_pkg-0.9.1.tar.gz",
    ],
)

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

####################################################################################################
# aspect_rules_swc setup
####################################################################################################

http_archive(
    name = "aspect_rules_swc",
    sha256 = "8eb9e42ed166f20cacedfdb22d8d5b31156352eac190fc3347db55603745a2d8",
    strip_prefix = "rules_swc-1.1.0",
    url = "https://github.com/aspect-build/rules_swc/releases/download/v1.1.0/rules_swc-v1.1.0.tar.gz",
)

load("@aspect_rules_swc//swc:dependencies.bzl", "rules_swc_dependencies")

rules_swc_dependencies()

load("@aspect_rules_swc//swc:repositories.bzl", "LATEST_SWC_VERSION", "swc_register_toolchains")

swc_register_toolchains(
    name = "swc",
    swc_version = LATEST_SWC_VERSION,
)

####################################################################################################
# rules_multirun setup
####################################################################################################

http_archive(
    name = "rules_multirun",
    sha256 = "9cd384e42b2da00104f0e18f25e66285aa21f64b573c667638a7a213206885ab",
    strip_prefix = "rules_multirun-0.6.1",
    url = "https://github.com/keith/rules_multirun/archive/refs/tags/0.6.1.tar.gz",
)
