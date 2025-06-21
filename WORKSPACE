# Declares that this directory is the root of a Bazel workspace.
# See https://docs.bazel.build/versions/main/build-ref.html#workspace
workspace(
    # How this workspace would be referenced with absolute labels from another workspace
    name = "lab",
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

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
