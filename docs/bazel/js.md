<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# JavaScript

Shared JavaScript wrappers keep external rules in one place.

<a id="js_binary"></a>

## js_binary

```starlark
load("//tools/bazel:js.bzl", "js_binary")

js_binary(name, **kwargs)
```

Declare a [js_binary](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_binary.bzl) with the upstream attributes.

**PARAMETERS**

| Name                                | Description                                          | Default Value |
| :---------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="js_binary-name"></a>name     | Target name.                                         | none          |
| <a id="js_binary-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="js_image_layer"></a>

## js_image_layer

```starlark
load("//tools/bazel:js.bzl", "js_image_layer")

js_image_layer(name, **kwargs)
```

Declare a [js_image_layer](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_image_layer.bzl) with the upstream attributes.

**PARAMETERS**

| Name                                     | Description                                          | Default Value |
| :--------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="js_image_layer-name"></a>name     | Target name.                                         | none          |
| <a id="js_image_layer-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="js_library"></a>

## js_library

```starlark
load("//tools/bazel:js.bzl", "js_library")

js_library(name, **kwargs)
```

Declare a [js_library](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_library.bzl) with the upstream attributes.

**PARAMETERS**

| Name                                 | Description                                          | Default Value |
| :----------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="js_library-name"></a>name     | Target name.                                         | none          |
| <a id="js_library-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="js_run_binary"></a>

## js_run_binary

```starlark
load("//tools/bazel:js.bzl", "js_run_binary")

js_run_binary(name, **kwargs)
```

Declare a [js_run_binary](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_run_binary.bzl) with the upstream attributes.

**PARAMETERS**

| Name                                    | Description                                          | Default Value |
| :-------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="js_run_binary-name"></a>name     | Target name.                                         | none          |
| <a id="js_run_binary-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="js_run_devserver"></a>

## js_run_devserver

```starlark
load("//tools/bazel:js.bzl", "js_run_devserver")

js_run_devserver(name, **kwargs)
```

Declare a [js_run_devserver](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_run_devserver.bzl) with the upstream attributes.

**PARAMETERS**

| Name                                       | Description                                          | Default Value |
| :----------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="js_run_devserver-name"></a>name     | Target name.                                         | none          |
| <a id="js_run_devserver-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="js_test"></a>

## js_test

```starlark
load("//tools/bazel:js.bzl", "js_test")

js_test(name, **kwargs)
```

Declare a [js_test](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_binary.bzl) with the upstream attributes.

**PARAMETERS**

| Name                              | Description                                          | Default Value |
| :-------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="js_test-name"></a>name     | Target name.                                         | none          |
| <a id="js_test-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |
