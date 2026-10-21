"""# OCI images and delivery

Shared OCI image creation, local loading and remote delivery.
"""

load("@aspect_bazel_lib//lib:expand_template.bzl", "expand_template")
load("@bazel_skylib//rules:write_file.bzl", "write_file")
load("@rules_oci//oci:defs.bzl", "oci_image_index", "oci_load", "oci_push", _oci_image = "oci_image")
load("@rules_shell//shell:sh_binary.bzl", "sh_binary")

def oci_deliver(image, repo_suffix, name = "deliver", visibility = ["//visibility:private"]):
    """Bazel macro for delivering oci images to a local and remote repository.

    Every target is prefixed with `name`, so a package can deliver more than one image.

    See:
    - https://docs.aspect.build/guides/delivery/
    - https://github.com/bazel-contrib/rules_oci/blob/main/docs/load.md
    - https://github.com/bazel-contrib/rules_oci/blob/main/docs/push.md

    ### Targets

    - `:[name].load` - oci_load target, with stamping configured for the tags.
    - `:[name].tar` - OCI-format tarball of the oci_load target.
    - `:[name].index` - image index that the tarball loads, for reading the image ID.
    - `:[name].push` - oci_push target, with stamping configured for the tags.
    - `:[name].check_then_push` - runs `:[name].push` only when the image is not already pushed.

    Args:
      image: Image to pass to oci_image.

      repo_suffix: Suffix to be applied after jackvincent/lab-.

      name: Prefix for the macro's targets.

      visibility: Visibility of tarball, typically for use in e2e testing.
    """

    repo_prefix = "lab"
    repository_name = "{0}-{1}".format(repo_prefix, repo_suffix)
    repository = "jackvincent/{0}".format(repository_name)

    tags_tmpl_label = "_%s_tags_tmpl" % name
    stamped_tags_label = "_%s_stamped_tags" % name

    write_file(
        name = tags_tmpl_label,
        out = "_%s_tags.txt.tmpl" % name,
        # Docker's containerd image store looks up an OCI tarball's tag only by
        # its fully qualified name.
        content = ["docker.io/{0}:VERSION".format(repository)],
    )

    expand_template(
        name = stamped_tags_label,
        out = "_%s_stamped.tags.txt" % name,
        stamp_substitutions = {"VERSION": "{{STABLE_VERSION}}"},
        substitutions = {"VERSION": "latest"},
        template = ":%s" % tags_tmpl_label,
    )

    index_label = "%s.index" % name

    # rules_oci writes an OCI-format tarball only from an image index.
    oci_image_index(
        name = index_label,
        images = [image],
        visibility = visibility,
    )

    load_label = "%s.load" % name

    # Tag and run a local container with:
    # $ bazel run :[name].load
    # $ docker run --rm jackvincent/lab-<repo_suffix>:latest
    oci_load(
        name = load_label,
        format = "oci",
        image = ":%s" % index_label,
        repo_tags = ":%s" % stamped_tags_label,
        visibility = visibility,
    )
    native.filegroup(
        name = "%s.tar" % name,
        srcs = [":%s" % load_label],
        output_group = "tarball",
        visibility = visibility,
    )

    version_tmpl_label = "_%s_version_tmpl" % name
    stamped_version_label = "_%s_stamped_version" % name

    write_file(
        name = version_tmpl_label,
        out = "_%s_version.txt.tmpl" % name,
        content = [
            "latest",
            "VERSION",
        ],
    )

    expand_template(
        name = stamped_version_label,
        out = "_%s_stamped.version.txt" % name,
        stamp_substitutions = {"VERSION": "{{STABLE_VERSION}}"},
        substitutions = {"VERSION": "0.0.0"},
        template = ":%s" % version_tmpl_label,
    )

    push_label = "%s.push" % name

    # Push and tag remote container with:
    # $ bazel run :[name].push
    # Note: requires prior docker login
    oci_push(
        name = push_label,
        image = image,
        remote_tags = ":%s" % stamped_version_label,
        repository = repository,
    )

    # First checks if a manifest has been previously pushed before executing the push above.
    # Useful to avoid re-tagging images that haven't changed.
    sh_binary(
        name = "%s.check_then_push" % name,
        srcs = ["//tools/bazel/oci:check_then_push.sh"],
        args = [
            "--crane_path $(location @lab_oci_crane_toolchains//:current_toolchain)",
            "--jq_path $(location @jq_toolchains//:resolved_toolchain)",
            "--image_dir $(location %s)" % image,
            "--repository %s" % repository,
            "--pusher_path $(location :%s)" % push_label,
        ],
        data = [
            "@lab_oci_crane_toolchains//:current_toolchain",
            "@jq_toolchains//:resolved_toolchain",
            image,
            ":%s" % push_label,
        ],
        tags = ["deliverable"],
    )

def oci_image(name, **kwargs):
    """Declare an [OCI image](https://github.com/bazel-contrib/rules_oci/blob/main/docs/image.md) with the upstream attributes.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _oci_image(
        name = name,
        **kwargs
    )
