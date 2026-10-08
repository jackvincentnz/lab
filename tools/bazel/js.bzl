"""# JavaScript

Shared JavaScript wrappers keep external rules in one place.
"""

load(
    "@aspect_rules_js//js:defs.bzl",
    _js_binary = "js_binary",
    _js_image_layer = "js_image_layer",
    _js_library = "js_library",
    _js_run_binary = "js_run_binary",
    _js_run_devserver = "js_run_devserver",
    _js_test = "js_test",
)

def js_library(name, **kwargs):
    """Declare a [js_library](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_library.bzl) with the upstream attributes.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _js_library(
        name = name,
        **kwargs
    )

def js_run_binary(name, **kwargs):
    """Declare a [js_run_binary](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_run_binary.bzl) with the upstream attributes.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _js_run_binary(
        name = name,
        **kwargs
    )

def js_run_devserver(name, **kwargs):
    """Declare a [js_run_devserver](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_run_devserver.bzl) with the upstream attributes.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _js_run_devserver(
        name = name,
        **kwargs
    )

def js_test(name, **kwargs):
    """Declare a [js_test](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_binary.bzl) with the upstream attributes.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _js_test(
        name = name,
        **kwargs
    )

def js_binary(name, **kwargs):
    """Declare a [js_binary](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_binary.bzl) with the upstream attributes.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _js_binary(
        name = name,
        **kwargs
    )

def js_image_layer(name, **kwargs):
    """Declare a [js_image_layer](https://github.com/aspect-build/rules_js/blob/v3.5.1/js/private/js_image_layer.bzl) with the upstream attributes.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _js_image_layer(
        name = name,
        **kwargs
    )
