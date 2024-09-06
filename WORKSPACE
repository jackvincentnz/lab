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

####################################################################################################
# rules_cypress setup
####################################################################################################

http_archive(
    name = "aspect_rules_cypress",
    sha256 = "bca909724a07bd52c9c0b01b461e1df019c3af28aecfd98de924c1d7c358166e",
    strip_prefix = "rules_cypress-0.5.0",
    url = "https://github.com/aspect-build/rules_cypress/releases/download/v0.5.0/rules_cypress-v0.5.0.tar.gz",
)

load("@aspect_rules_cypress//cypress:dependencies.bzl", "rules_cypress_dependencies")

rules_cypress_dependencies()

load("@aspect_rules_cypress//cypress:repositories.bzl", "cypress_register_toolchains")

# To update integrity, run the following before chosen cypress_version update:
# bazel run @aspect_rules_cypress//scripts:mirror_releases <cypress_version>
cypress_register_toolchains(
    name = "cypress",
    cypress_integrity = {
        "darwin-x64": "4804714d8e0ce3054023eb304fce059eb69f62439be369e8346764715c4d24ae",
        "darwin-arm64": "4ac480a87d78f7420a5bb44060004f5c9e76127767102ffeccb647292dd8bc4b",
        "linux-x64": "6a1a47771870b49383e3a92f1ca8a53f3c222ba15265652e98e445daaf101208",
        "linux-arm64": "4961adcf1da1cffa0c3d1b9cbc96c359474a7b6140f357d1fc7c30d4741980d3",
        "win32-x64": "5bd98a620f80b5928fc097f5e08e2c1cf9a8e092a43b90e392f686bd53e88694",
    },
    cypress_version = "13.13.0",
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
