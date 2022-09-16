# Third-party dependencies fetched by Bazel
# Unlike WORKSPACE, the content of this file is unordered.
# We keep them separate to make the WORKSPACE file more maintainable.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def fetch_dependencies():
    http_archive(
        name = "aspect_rules_js",
        sha256 = "bc9d48758458d72e743f6de4c485482e122b3b4472878cf6ff83bd0caa4ab855",
        strip_prefix = "rules_js-1.3.0",
        url = "https://github.com/aspect-build/rules_js/archive/refs/tags/v1.3.0.tar.gz",
    )

    http_archive(
        name = "aspect_rules_ts",
        sha256 = "3eb3171c26eb5d0951d51ae594695397218fb829e3798eea5557814723a1b3da",
        strip_prefix = "rules_ts-1.0.0-rc3",
        url = "https://github.com/aspect-build/rules_ts/archive/refs/tags/v1.0.0-rc3.tar.gz",
    )

    http_archive(
        name = "aspect_rules_jasmine",
        sha256 = "938a2818100fd89e7600a45b7ba4fcd4114c11c5b5741db30ff7c6e0dcb2ea4b",
        strip_prefix = "rules_jasmine-0.1.0",
        url = "https://github.com/aspect-build/rules_jasmine/archive/refs/tags/v0.1.0.tar.gz",
    )
