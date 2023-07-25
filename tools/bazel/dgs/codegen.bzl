"""
Rule for generate dgs java types from a graphql schema
"""

def _dgs_codegen(ctx):
    target_dir = ctx.actions.declare_directory(ctx.attr.name + "_generated")

    args = ctx.actions.args()
    args.add("--output-dir", target_dir.path)
    args.add("--package-name", ctx.attr.package_name)
    args.add("--generate-client")

    for schema in ctx.files.schemas:
        args.add(schema.path)

    ctx.actions.run(
        mnemonic = "DgsCodegen",
        executable = ctx.executable._codegen_binary,
        arguments = [args],
        inputs = ctx.files.schemas,
        outputs = [target_dir],
    )

    srcjar = ctx.actions.declare_file(ctx.attr.name + "_generated.srcjar")

    ctx.actions.run_shell(
        inputs = [target_dir],
        tools = [ctx.executable._zipper],
        outputs = [srcjar],
        command = "pushd {dir}; zip -X -q {compress} -r ../{outname} .".format(
            outname = srcjar.basename,
            dir = target_dir.path,
            zipper = ctx.executable._zipper.path,
            compress = "-0",
        ),
        use_default_shell_env = False,
        progress_message = "Zipping %s ..." % ctx.attr.name,
    )

    return [
        DefaultInfo(
            files = depset([srcjar]),
        ),
    ]

dgs_codegen = rule(
    implementation = _dgs_codegen,
    attrs = {
        "schemas": attr.label_list(
            mandatory = True,
            allow_empty = False,
            allow_files = [".graphqls"],
        ),
        "package_name": attr.string(
            mandatory = True,
        ),
        "_codegen_binary": attr.label(
            default = Label("//tools/bazel/dgs:codegen"),
            executable = True,
            cfg = "exec",
        ),
        "_zipper": attr.label(
            executable = True,
            cfg = "exec",
            default = Label("@bazel_tools//tools/zip:zipper"),
            allow_files = True,
        ),
    },
    output_to_genfiles = True,
)

def dgs_codegen_library(name, **kwargs):
    visibility = kwargs.pop("visibility", ["//visibility:private"])

    dgs_codegen(
        name = name + "_srcs",
        **kwargs
    )

    native.java_library(
        name = name,
        srcs = [":" + name + "_srcs"],
        deps = ["@maven//:com_netflix_graphql_dgs_codegen_graphql_dgs_codegen_shared_core"],
        visibility = visibility,
    )
