"""
Rule which first checks if an image exists in the repository before executing the pusher.
"""

def _push_if_not_exists_impl(ctx):
    crane = ctx.toolchains["@rules_oci//oci:crane_toolchain_type"]
    yq = ctx.toolchains["@aspect_bazel_lib//lib:yq_toolchain_type"]
    pusher = ctx.attr.pusher[DefaultInfo]

    executable = ctx.actions.declare_file("push_if_not_exists_%s.sh" % ctx.label.name)
    files = [ctx.file.image]
    substitutions = {
        "{{crane_path}}": crane.crane_info.binary.short_path,
        "{{yq_path}}": yq.yqinfo.bin.short_path,
        "{{image_dir}}": ctx.file.image.short_path,
        "{{repository}}": ctx.attr.repository,
        "{{pusher_path}}": ctx.executable.pusher.short_path,
    }

    ctx.actions.expand_template(
        template = ctx.file._check_if_manifest_exists_sh_tpl,
        output = executable,
        is_executable = True,
        substitutions = substitutions,
    )

    runfiles = ctx.runfiles(files = files)
    runfiles = runfiles.merge(yq.default.default_runfiles)
    runfiles = runfiles.merge(crane.default.default_runfiles)
    runfiles = runfiles.merge(pusher.default_runfiles)

    return DefaultInfo(executable = executable, runfiles = runfiles)

push_if_not_exists = rule(
    implementation = _push_if_not_exists_impl,
    attrs = {
        "image": attr.label(
            allow_single_file = True,
            doc = "Label to an oci_image or oci_image_index",
            mandatory = True,
        ),
        "repository": attr.string(),
        "pusher": attr.label(
            executable = True,
            cfg = "exec",
        ),
        "_check_if_manifest_exists_sh_tpl": attr.label(
            default = "push_if_not_exists.sh.tpl",
            allow_single_file = True,
        ),
    },
    toolchains = [
        "@rules_oci//oci:crane_toolchain_type",
        "@aspect_bazel_lib//lib:yq_toolchain_type",
    ],
    executable = True,
)
