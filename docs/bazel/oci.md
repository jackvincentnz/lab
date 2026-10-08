<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# OCI images and delivery

Shared OCI image creation, local loading and remote delivery.

<a id="oci_deliver"></a>

## oci_deliver

```starlark
load("//tools/bazel/oci:defs.bzl", "oci_deliver")

oci_deliver(name, image, repo_suffix, visibility)
```

Bazel macro for delivering oci images to a local and remote repository.

See:

- https://docs.aspect.build/guides/delivery/
- https://github.com/bazel-contrib/rules_oci/blob/main/docs/load.md
- https://github.com/bazel-contrib/rules_oci/blob/main/docs/push.md

### Targets

- `:[name].load` - oci_load target, with stamping configured for the tags.
- `:[name].tar` - OCI-format tarball of the oci_load target.
- `:[name].index` - image index that the tarball loads, for reading the image ID.
- `:push` - oci_push target, with stamping configured for the tags.

**PARAMETERS**

| Name                                            | Description                                              | Default Value              |
| :---------------------------------------------- | :------------------------------------------------------- | :------------------------- |
| <a id="oci_deliver-name"></a>name               | Macro name.                                              | none                       |
| <a id="oci_deliver-image"></a>image             | Image to pass to oci_image.                              | none                       |
| <a id="oci_deliver-repo_suffix"></a>repo_suffix | Suffix to be applied after jackvincent/lab-.             | none                       |
| <a id="oci_deliver-visibility"></a>visibility   | Visibility of tarball, typically for use in e2e testing. | `["//visibility:private"]` |

<a id="oci_image"></a>

## oci_image

```starlark
load("//tools/bazel/oci:defs.bzl", "oci_image")

oci_image(name, **kwargs)
```

Declare an [OCI image](https://github.com/bazel-contrib/rules_oci/blob/v2.3.0/docs/image.md) with the upstream attributes.

**PARAMETERS**

| Name                                | Description                                          | Default Value |
| :---------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="oci_image-name"></a>name     | Target name.                                         | none          |
| <a id="oci_image-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |
