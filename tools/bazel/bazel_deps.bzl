# Third-party dependencies fetched by Bazel
# Unlike WORKSPACE, the content of this file is unordered.
# We keep them separate to make the WORKSPACE file more maintainable.

# Install the nodejs "bootstrap" package
# This provides the basic tools for running and packaging nodejs programs in Bazel
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
def fetch_dependencies():
    http_archive(
        name = "build_bazel_rules_nodejs",
        sha256 = "a09edc4ba3931a856a5ac6836f248c302d55055d35d36e390a0549799c33145b",
        urls = ["https://github.com/bazelbuild/rules_nodejs/releases/download/5.0.2/rules_nodejs-5.0.2.tar.gz"],
    )
