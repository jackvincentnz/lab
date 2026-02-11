"""
Rule for generate dgs java types from a graphql schema
"""

def _zipper_input_path(file):
    marker = "_generated/"
    marker_index = file.path.find(marker)
    if marker_index == -1:
        fail("expected generated file path to include '%s': %s" % (marker, file.path))
    zip_path = file.path[marker_index + len(marker):]
    return "{}={}".format(zip_path, file.path)

def _dgs_codegen(ctx):
    target_dir = ctx.actions.declare_directory(ctx.attr.name + "_generated")

    args = ctx.actions.args()
    args.add("--output-dir", target_dir.path)
    args.add("--package-name", ctx.attr.package_name)
    args.add("--generate-client")
    args.add("--type-mapping", "BigDecimal=java.math.BigDecimal")

    for schema in ctx.files.schemas:
        args.add(schema.path)

    ctx.actions.run_shell(
        mnemonic = "DgsCodegen",
        command = """
set +e
output="$({codegen} \"$@\" 2>&1)"
exit_code=$?
if [ "$exit_code" -ne 0 ]; then
    printf '%s\n' "$output" >&2
fi
exit "$exit_code"
""".format(
            codegen = ctx.executable._codegen_binary.path,
        ),
        arguments = [args],
        inputs = ctx.files.schemas,
        tools = [ctx.executable._codegen_binary],
        outputs = [target_dir],
    )

    srcjar = ctx.actions.declare_file(ctx.attr.name + "_generated.srcjar")

    zipper_args = ctx.actions.args()
    zipper_args.add("cC")
    zipper_args.add(srcjar.path)

    zipper_inputs = ctx.actions.args()
    zipper_inputs.add_all(
        [target_dir],
        expand_directories = True,
        map_each = _zipper_input_path,
    )

    ctx.actions.run(
        inputs = [target_dir],
        tools = [ctx.executable._zipper],
        outputs = [srcjar],
        executable = ctx.executable._zipper,
        arguments = [zipper_args, zipper_inputs],
        mnemonic = "ZipSrcjar",
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
            default = Label("@bazel_tools//tools/zip:zipper"),
            executable = True,
            cfg = "exec",
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
        deps = [
            "@maven//:com_netflix_graphql_dgs_codegen_graphql_dgs_codegen_shared_core",
            "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        ],
        visibility = visibility,
    )
