"""
This module contains common oci macros.
"""

load("@bazel_skylib//rules:write_file.bzl", "write_file")
load("@aspect_bazel_lib//lib:expand_template.bzl", "expand_template")
load("@rules_oci//oci:defs.bzl", "oci_load", "oci_push", _oci_image = "oci_image")

def oci_deliver(name, image, repo_suffix, visibility = ["//visibility:private"]):
    """Bazel macro for delivering oci images to a local and remote repository.

    See:
    - https://docs.aspect.build/guides/delivery/
    - https://github.com/bazel-contrib/rules_oci/blob/main/docs/load.md
    - https://github.com/bazel-contrib/rules_oci/blob/main/docs/push.md

    ### Targets

    - `:[name].load` - oci_load target, with stamping configured for the tags.
    - `:push` - oci_push target, with stamping configured for the tags.

    Args:
      name: Macro name.

      image: Image to pass to oci_image.

      repo_suffix: Suffix to be applied after jackvincent/lab-.

      visibility: Visibility of tarball, typically for use in e2e testing.
    """

    repo_prefix = "lab"
    repository_name = "{0}-{1}".format(repo_prefix, repo_suffix)
    repository = "jackvincent/{0}".format(repository_name)

    write_file(
        name = "_tags_tmpl",
        out = "_tags.txt.tmpl",
        content = ["{0}:VERSION".format(repository)],
    )

    expand_template(
        name = "_stamped_tags",
        out = "_stamped.tags.txt",
        stamp_substitutions = {"VERSION": "{{STABLE_VERSION}}"},
        substitutions = {"VERSION": "latest"},
        template = ":_tags_tmpl",
    )

    load_label = "%s.load" % name

    # Tag and run a local container with:
    # $ bazel run :[oci_deliver name]
    # $ docker run --rm jackvincent/lab-<repo_suffix>:latest
    oci_load(
        name = load_label,
        image = image,
        repo_tags = ":_stamped_tags",
        visibility = visibility,
    )
    native.alias(
        name = name,
        actual = load_label,
        visibility = visibility,
    )
    native.filegroup(
        name = "%s.tar" % name,
        srcs = [":%s" % name],
        output_group = "tarball",
        visibility = visibility,
    )

    write_file(
        name = "_version_tmpl",
        out = "_version.txt.tmpl",
        content = [
            "latest",
            "VERSION",
        ],
    )

    expand_template(
        name = "_stamped_version",
        out = "_stamped.version.txt",
        stamp_substitutions = {"VERSION": "{{STABLE_VERSION}}"},
        substitutions = {"VERSION": "0.0.0"},
        template = ":_version_tmpl",
    )

    # Push and tag remote container with:
    # $ bazel run :push
    # Note: requires prior docker login
    oci_push(
        name = "push",
        image = image,
        remote_tags = ":_stamped_version",
        repository = repository,
    )

    # First checks if a manifest has been previously pushed before executing :push above.
    # Useful to avoid re-tagging images that haven't changed.
    native.sh_binary(
        name = "check_then_push",
        srcs = ["//tools/bazel/oci:check_then_push.sh"],
        args = [
            "--crane_path $(location @oci_crane_toolchains//:current_toolchain)",
            "--yq_path $(location @jq_toolchains//:resolved_toolchain)",
            "--image_dir $(location %s)" % image,
            "--repository %s" % repository,
            "--pusher_path $(location :push)",
        ],
        data = [
            "@oci_crane_toolchains//:current_toolchain",
            "@jq_toolchains//:resolved_toolchain",
            image,
            ":push",
        ],
        tags = ["deliverable"],
    )

def oci_image(name, **kwargs):
    _oci_image(
        name = name,
        **kwargs
    )
