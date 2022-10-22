# Third-party dependencies fetched by Bazel
# Unlike WORKSPACE, the content of this file is unordered.
# We keep them separate to make the WORKSPACE file more maintainable.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def fetch_dependencies():
    http_archive(
        name = "aspect_rules_js",
        sha256 = "b9fde0f20de6324ad443500ae738bda00facbd73900a12b417ce794856e01407",
        strip_prefix = "rules_js-1.5.0",
        url = "https://github.com/aspect-build/rules_js/archive/refs/tags/v1.5.0.tar.gz",
    )

    http_archive(
        name = "aspect_rules_ts",
        sha256 = "743f0e988e4e3f1e25e52c79f9dc3da1ddd77507ae88787ae95b4e70c537872b",
        strip_prefix = "rules_ts-1.0.0-rc4",
        url = "https://github.com/aspect-build/rules_ts/archive/refs/tags/v1.0.0-rc4.tar.gz",
    )

    http_archive(
        name = "aspect_rules_jasmine",
        sha256 = "938a2818100fd89e7600a45b7ba4fcd4114c11c5b5741db30ff7c6e0dcb2ea4b",
        strip_prefix = "rules_jasmine-0.1.0",
        url = "https://github.com/aspect-build/rules_jasmine/archive/refs/tags/v0.1.0.tar.gz",
    )

    http_archive(
        name = "io_bazel_rules_go",
        sha256 = "099a9fb96a376ccbbb7d291ed4ecbdfd42f6bc822ab77ae6f1b5cb9e914e94fa",
        urls = [
            "https://mirror.bazel.build/github.com/bazelbuild/rules_go/releases/download/v0.35.0/rules_go-v0.35.0.zip",
            "https://github.com/bazelbuild/rules_go/releases/download/v0.35.0/rules_go-v0.35.0.zip",
        ],
    )

    http_archive(
        name = "bazel_gazelle",
        sha256 = "efbbba6ac1a4fd342d5122cbdfdb82aeb2cf2862e35022c752eaddffada7c3f3",
        urls = [
            "https://mirror.bazel.build/github.com/bazelbuild/bazel-gazelle/releases/download/v0.27.0/bazel-gazelle-v0.27.0.tar.gz",
            "https://github.com/bazelbuild/bazel-gazelle/releases/download/v0.27.0/bazel-gazelle-v0.27.0.tar.gz",
        ],
    )
