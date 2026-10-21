<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# OCI images and delivery

Shared OCI image creation, local loading and remote delivery.

<a id="oci_deliver"></a>

## oci_deliver

<pre>
load("//tools/bazel/oci:defs.bzl", "oci_deliver")

oci_deliver(<a href="#oci_deliver-image">image</a>, <a href="#oci_deliver-repo_suffix">repo_suffix</a>, <a href="#oci_deliver-name">name</a>, <a href="#oci_deliver-visibility">visibility</a>)
</pre>

Bazel macro for delivering oci images to a local and remote repository.

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


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="oci_deliver-image"></a>image |  Image to pass to oci_image.   |  none |
| <a id="oci_deliver-repo_suffix"></a>repo_suffix |  Suffix to be applied after jackvincent/lab-.   |  none |
| <a id="oci_deliver-name"></a>name |  Prefix for the macro's targets.   |  `"deliver"` |
| <a id="oci_deliver-visibility"></a>visibility |  Visibility of tarball, typically for use in e2e testing.   |  `["//visibility:private"]` |


<a id="oci_image"></a>

## oci_image

<pre>
load("//tools/bazel/oci:defs.bzl", "oci_image")

oci_image(<a href="#oci_image-name">name</a>, <a href="#oci_image-kwargs">**kwargs</a>)
</pre>

Declare an [OCI image](https://github.com/bazel-contrib/rules_oci/blob/main/docs/image.md) with the upstream attributes.

**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="oci_image-name"></a>name |  Target name.   |  none |
| <a id="oci_image-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


