"""
Third-party dependencies fetched by Bazel
Unlike WORKSPACE, the content of this file is unordered.
We keep them separate to make the WORKSPACE file more maintainable.
"""

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "5.3"
RULES_JVM_EXTERNAL_SHA = "d31e369b854322ca5098ea12c69d7175ded971435e55c18dd9dd5f29cc5249ac"

def fetch_dependencies():
    """
    Fetches all the third party archives needed for this workspace.
    """
    http_archive(
        name = "rules_jvm_external",
        strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
        sha256 = RULES_JVM_EXTERNAL_SHA,
        url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/%s/rules_jvm_external-%s.tar.gz" % (RULES_JVM_EXTERNAL_TAG, RULES_JVM_EXTERNAL_TAG),
    )

    http_archive(
        name = "contrib_rules_jvm",
        sha256 = "159d343f799e4d18a51096c9d6298982fc42d9e67a7e0f8f10862e2a7add580b",
        strip_prefix = "rules_jvm-0.16.0",
        url = "https://github.com/bazel-contrib/rules_jvm/releases/download/v0.16.0/rules_jvm-v0.16.0.tar.gz",
    )

    http_archive(
        name = "aspect_rules_ts",
        sha256 = "ace5b609603d9b5b875d56c9c07182357c4ee495030f40dcefb10d443ba8c208",
        strip_prefix = "rules_ts-1.4.0",
        url = "https://github.com/aspect-build/rules_ts/releases/download/v1.4.0/rules_ts-v1.4.0.tar.gz",
    )

    http_archive(
        name = "aspect_rules_jest",
        sha256 = "a2f35eedce1a59f94b2158b056caab09ed66b92cafabd631b25dac78a178e044",
        strip_prefix = "rules_jest-0.17.0",
        url = "https://github.com/aspect-build/rules_jest/releases/download/v0.17.0/rules_jest-v0.17.0.tar.gz",
    )

    http_archive(
        name = "aspect_rules_jasmine",
        sha256 = "b3b2ff30ed222db653092d8280e0b62a4d54c5e65c598df09a0a1d7aae78fc8f",
        strip_prefix = "rules_jasmine-0.3.1",
        url = "https://github.com/aspect-build/rules_jasmine/releases/download/v0.3.1/rules_jasmine-v0.3.1.tar.gz",
    )

    http_archive(
        name = "io_bazel_rules_go",
        sha256 = "dd926a88a564a9246713a9c00b35315f54cbd46b31a26d5d8fb264c07045f05d",
        urls = [
            "https://mirror.bazel.build/github.com/bazelbuild/rules_go/releases/download/v0.38.1/rules_go-v0.38.1.zip",
            "https://github.com/bazelbuild/rules_go/releases/download/v0.38.1/rules_go-v0.38.1.zip",
        ],
    )

    http_archive(
        name = "bazel_gazelle",
        sha256 = "ecba0f04f96b4960a5b250c8e8eeec42281035970aa8852dda73098274d14a1d",
        urls = [
            "https://mirror.bazel.build/github.com/bazelbuild/bazel-gazelle/releases/download/v0.29.0/bazel-gazelle-v0.29.0.tar.gz",
            "https://github.com/bazelbuild/bazel-gazelle/releases/download/v0.29.0/bazel-gazelle-v0.29.0.tar.gz",
        ],
    )
