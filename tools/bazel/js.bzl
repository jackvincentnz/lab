"""
This module contains common js macros to avoid direct dependencies on external rules.
"""

load(
    "@aspect_rules_js//js:defs.bzl",
    _js_binary = "js_binary",
    _js_library = "js_library",
    _js_run_binary = "js_run_binary",
    _js_run_devserver = "js_run_devserver",
    _js_test = "js_test",
)

def js_library(name, **kwargs):
    _js_library(
        name = name,
        **kwargs
    )

def js_run_binary(name, **kwargs):
    _js_run_binary(
        name = name,
        **kwargs
    )

def js_run_devserver(name, **kwargs):
    _js_run_devserver(
        name = name,
        **kwargs
    )

def js_test(name, **kwargs):
    _js_test(
        name = name,
        **kwargs
    )

def js_binary(name, **kwargs):
    _js_binary(
        name = name,
        **kwargs
    )
