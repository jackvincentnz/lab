# Declares that this directory is the root of a Bazel workspace.
# See https://docs.bazel.build/versions/main/build-ref.html#workspace
workspace(
    # How this workspace would be referenced with absolute labels from another workspace
    name = "lab",
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

####################################################################################################
# aspect_bazel_lib setup
####################################################################################################

http_archive(
    name = "aspect_bazel_lib",
    sha256 = "bda4a69fa50411b5feef473b423719d88992514d259dadba7d8218a1d02c7883",
    strip_prefix = "bazel-lib-2.3.0",
    url = "https://github.com/aspect-build/bazel-lib/releases/download/v2.3.0/bazel-lib-v2.3.0.tar.gz",
)

load(
    "@aspect_bazel_lib//lib:repositories.bzl",
    "aspect_bazel_lib_dependencies",
    "aspect_bazel_lib_register_toolchains",
    "register_coreutils_toolchains",
)

aspect_bazel_lib_dependencies()

aspect_bazel_lib_register_toolchains()

register_coreutils_toolchains()

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
    sha256 = "2412e22bc1eb9d3a5eae15180f304140f1aad3f8184dbd99c845fafde0964559",
    strip_prefix = "rules_jvm-0.24.0",
    url = "https://github.com/bazel-contrib/rules_jvm/releases/download/v0.24.0/rules_jvm-v0.24.0.tar.gz",
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
    sha256 = "7bb891ccb2f53ca188a769b3a3777be1c38348e18091afea05320f3003b3e886",
    urls = [
        "https://github.com/salesforce/rules_spring/releases/download/2.3.1/rules-spring-2.3.1.zip",
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
    sha256 = "6ad28b5bac2bb5a74e737925fbc3f62ce1edabe5a48d61a9980c491ef4cedfb7",
    strip_prefix = "rules_ts-2.1.1",
    url = "https://github.com/aspect-build/rules_ts/releases/download/v2.1.1/rules_ts-v2.1.1.tar.gz",
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
    sha256 = "6734a719993b1ba4ebe9806e853864395a8d3968ad27f9dd759c196b3eb3abe8",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_go/releases/download/v0.45.1/rules_go-v0.45.1.zip",
        "https://github.com/bazelbuild/rules_go/releases/download/v0.45.1/rules_go-v0.45.1.zip",
    ],
)

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")

go_rules_dependencies()

go_register_toolchains(version = "1.19.5")

http_archive(
    name = "bazel_gazelle",
    sha256 = "32938bda16e6700063035479063d9d24c60eda8d79fd4739563f50d331cb3209",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-gazelle/releases/download/v0.35.0/bazel-gazelle-v0.35.0.tar.gz",
        "https://github.com/bazelbuild/bazel-gazelle/releases/download/v0.35.0/bazel-gazelle-v0.35.0.tar.gz",
    ],
)

load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")

gazelle_dependencies()

####################################################################################################
# rules_oci setup
####################################################################################################

http_archive(
    name = "rules_oci",
    sha256 = "58b7a175ee90c12583afeca388523adf6a4e5a0528f330b41c302b91a4d6fc06",
    strip_prefix = "rules_oci-1.6.0",
    url = "https://github.com/bazel-contrib/rules_oci/releases/download/v1.6.0/rules_oci-v1.6.0.tar.gz",
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
    digest = "sha256:161ef4b1bf7effb350a2a9625cb2b59f69d54ec6059a8a155a1438d0439c593c",
    image = "docker.io/library/nginx",
)

####################################################################################################
# rules_pkg setup
####################################################################################################

http_archive(
    name = "rules_pkg",
    sha256 = "e93b7309591cabd68828a1bcddade1c158954d323be2205063e718763627682a",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/releases/download/0.10.0/rules_pkg-0.10.0.tar.gz",
        "https://github.com/bazelbuild/rules_pkg/releases/download/0.10.0/rules_pkg-0.10.0.tar.gz",
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
# rules_cypress setup
####################################################################################################

http_archive(
    name = "aspect_rules_cypress",
    sha256 = "76947778d8e855eee3c15931e1fcdc1c2a25d56d6c0edd110b2227c05b794d08",
    strip_prefix = "rules_cypress-0.3.2",
    url = "https://github.com/aspect-build/rules_cypress/archive/refs/tags/v0.3.2/rules_cypress-v0.3.2.tar.gz",
)

load("@aspect_rules_cypress//cypress:dependencies.bzl", "rules_cypress_dependencies")
load("@aspect_rules_cypress//cypress:repositories.bzl", "cypress_register_toolchains")

rules_cypress_dependencies()

cypress_register_toolchains(
    name = "cypress",
    cypress_integrity = {
        "darwin-x64": "63cf64deb6a3b707d540aa574438f25792552948560371ca58ad1566db852525",
        "darwin-arm64": "81238d8c1128add2c5a27019225e789c941118ec91bc0107e9a3a2870ffb1599",
        "linux-x64": "c9711d018f5af0bcba369bdcb5637a0454ddad0bb52f6db880fd55bfbdefe1e1",
        "linux-arm64": "c6684aa037eb9bd4efa47cc4d37815637689ce7a84f5913a615eb515baabb8fe",
        "win32-x64": "73ae75233d888a36c4d94352e9e23694c4c49bafbc9598ee05d42353e6790693",
    },
    cypress_version = "12.17.4",
)

# To update CHROME_REVISION, use the below script
#
# LASTCHANGE_URL="https://www.googleapis.com/download/storage/v1/b/chromium-browser-snapshots/o/Linux_x64%2FLAST_CHANGE?alt=media"
# CHROME_REVISION=$(curl -s -S $LASTCHANGE_URL)
# echo "latest CHROME_REVISION_LINUX is $CHROME_REVISION"
CHROME_REVISION_LINUX = "1072361"

http_archive(
    name = "chrome_linux",
    build_file_content = """filegroup(
name = "all",
srcs = glob(["**"]),
visibility = ["//visibility:public"],
)""",
    sha256 = "0df22f743facd1e090eff9b7f8d8bdc293fb4dc31ce9156d2ef19b515974a72b",
    strip_prefix = "chrome-linux",
    urls = [
        "https://www.googleapis.com/download/storage/v1/b/chromium-browser-snapshots/o/Linux_x64%2F" + CHROME_REVISION_LINUX + "%2Fchrome-linux.zip?alt=media",
    ],
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
