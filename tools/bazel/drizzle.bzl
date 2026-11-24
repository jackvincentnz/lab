"""# Drizzle

Drizzle migration generation for a project's SQLite schema.
"""

load("@npm//:drizzle-kit/package_json.bzl", drizzle_kit_bin = "bin")

def drizzle_generate(name, data, **kwargs):
    """Declare a target that generates a migration with `bazel run`.

    Runs `drizzle-kit generate` in the calling package, which loads
    `drizzle.config.ts` from there. `bazel run` starts in the runfiles, so the
    config writes migrations under `BUILD_WORKSPACE_DIRECTORY`. Pass arguments
    after `--`, such as `--name=add_due_date`.

    Args:
        name: Target name.
        data: The config and the compiled schema it names.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    drizzle_kit_bin.drizzle_kit_binary(
        name = name,
        args = ["generate"],
        chdir = native.package_name(),
        data = data + [
            "//:node_modules/drizzle-kit",
            "//:node_modules/drizzle-orm",
        ],
        **kwargs
    )
