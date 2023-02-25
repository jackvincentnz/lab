# Declares that this directory is the root of a Bazel workspace.
# See https://docs.bazel.build/versions/main/build-ref.html#workspace
workspace(
    # How this workspace would be referenced with absolute labels from another workspace
    name = "lab",
)

load("//tools/bazel:bazel_deps.bzl", "fetch_dependencies")

fetch_dependencies()

###############
# maven setup #
###############

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "junit:junit:4.13.2",
        "org.springframework.boot:spring-boot-starter-web:3.0.3",
        "org.springframework.boot:spring-boot-starter-test:3.0.3",
    ],
    fetch_sources = True,
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)

#########################
# aspect_rules_ts setup #
#########################
load("@aspect_rules_ts//ts:repositories.bzl", "rules_ts_dependencies")

rules_ts_dependencies(
    ts_version_from = "//:package.json",
)

######################
# rules_nodejs setup #
######################
load("@rules_nodejs//nodejs:repositories.bzl", "DEFAULT_NODE_VERSION", "nodejs_register_toolchains")

nodejs_register_toolchains(
    name = "nodejs",
    node_version = DEFAULT_NODE_VERSION,
)

#########################
# aspect_rules_js setup #
#########################

load("@aspect_rules_js//npm:npm_import.bzl", "npm_translate_lock")

npm_translate_lock(
    name = "npm",
    pnpm_lock = "//:pnpm-lock.yaml",
    verify_node_modules_ignored = "//:.bazelignore",
)

load("@npm//:repositories.bzl", "npm_repositories")

npm_repositories()

##############################
# aspect_rules_jasmine setup #
##############################

load("@aspect_rules_jasmine//jasmine:repositories.bzl", "jasmine_repositories")

jasmine_repositories(name = "jasmine")

load("@jasmine//:npm_repositories.bzl", jasmine_npm_repositories = "npm_repositories")

jasmine_npm_repositories()

######################
# bazel_gazelle setup
######################

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")
load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")

############################################################
# Define your own dependencies here using go_repository.
# Else, dependencies declared by rules_go/gazelle will be used.
# The first declaration of an external repository "wins".
############################################################

go_rules_dependencies()

go_register_toolchains(version = "1.18.3")

gazelle_dependencies()
