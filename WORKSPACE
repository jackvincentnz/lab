# Declares that this directory is the root of a Bazel workspace.
# See https://docs.bazel.build/versions/main/build-ref.html#workspace
workspace(
    # How this workspace would be referenced with absolute labels from another workspace
    name = "lab",
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")

####################################################################################################
# rules_spring setup
####################################################################################################

http_archive(
    name = "rules_spring",
    sha256 = "87b337f95f9c09a2e5875f0bca533b050c9ccb8b0d2c92915e290520b79d0912",
    urls = [
        "https://github.com/salesforce/rules_spring/releases/download/2.3.2/rules-spring-2.3.2.zip",
    ],
)

http_jar(
    name = "opentelemetry-javaagent",
    sha256 = "b23e5ab7f5fb4f58826c6d2c31ea637825c7190652a9d47d86692e37e615a0d5",
    url = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.23.0/opentelemetry-javaagent.jar",
)
