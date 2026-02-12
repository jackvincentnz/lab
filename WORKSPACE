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
    sha256 = "2d0805b4096db89b8e407ed0c243ce81c3d20f346e4c259885041d5eabc59436",
    urls = [
        "https://github.com/salesforce/rules_spring/releases/download/2.6.3/rules-spring-2.6.3.zip",
    ],
)

http_jar(
    name = "opentelemetry-javaagent",
    sha256 = "b23e5ab7f5fb4f58826c6d2c31ea637825c7190652a9d47d86692e37e615a0d5",
    url = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.23.0/opentelemetry-javaagent.jar",
)
