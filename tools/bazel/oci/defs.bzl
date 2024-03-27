"""
This module contains common oci macros.
"""

load("@bazel_skylib//rules:write_file.bzl", "write_file")
load("@aspect_bazel_lib//lib:expand_template.bzl", "expand_template")
load("@rules_oci//oci:defs.bzl", "oci_push", "oci_tarball", _oci_image = "oci_image")
load(":push_if_not_exists.bzl", "push_if_not_exists")

def oci_deliver(name, image, repo_suffix, visibility = ["//visibility:private"]):
    """Bazel macro for delivering oci images to a local and remote repository.

    See:
    - https://docs.aspect.build/guides/delivery/
    - https://github.com/bazel-contrib/rules_oci/blob/main/docs/tarball.md
    - https://github.com/bazel-contrib/rules_oci/blob/main/docs/push.md

    ### Targets

    - `:tarball` - oci_tarball target, with stamping configured for the tags.
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

    # Tag and run a local container with:
    # $ bazel run :tarball
    # $ docker run --rm jackvincent/lab-<repo_suffix>:latest
    oci_tarball(
        name = "tarball",
        image = image,
        repo_tags = ":_stamped_tags",
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
    push_if_not_exists(
        name = "push_if_not_exists",
        image = image,
        pusher = ":push",
        repository = repository,
    )

def oci_image(name, **kwargs):
    _oci_image(
        name = name,
        **kwargs
    )
