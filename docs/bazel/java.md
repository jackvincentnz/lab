<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# Java

Shared Java wrappers keep external rules and test defaults in one place.

<a id="java_binary"></a>

## java_binary

```starlark
load("//tools/bazel:java.bzl", "java_binary")

java_binary(name, **kwargs)
```

Declare a Java binary with [rules_java](https://github.com/bazelbuild/rules_java/blob/9.9.0/java/defs.bzl).

**PARAMETERS**

| Name                                  | Description                                          | Default Value |
| :------------------------------------ | :--------------------------------------------------- | :------------ |
| <a id="java_binary-name"></a>name     | Target name.                                         | none          |
| <a id="java_binary-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="java_junit5_test"></a>

## java_junit5_test

```starlark
load("//tools/bazel:java.bzl", "java_junit5_test")

java_junit5_test(name, **kwargs)
```

Declare a [JUnit 5 test](https://github.com/bazel-contrib/rules_jvm/blob/v0.34.0/README.md#java_junit5_test).

Defaults to small size and the test Spring profile unless set explicitly,
and adds Logback to runtime dependencies.

**PARAMETERS**

| Name                                       | Description                                          | Default Value |
| :----------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="java_junit5_test-name"></a>name     | Target name.                                         | none          |
| <a id="java_junit5_test-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="java_library"></a>

## java_library

```starlark
load("//tools/bazel:java.bzl", "java_library")

java_library(name, **kwargs)
```

Declare a Java library with [rules_java](https://github.com/bazelbuild/rules_java/blob/9.9.0/java/defs.bzl).

**PARAMETERS**

| Name                                   | Description                                          | Default Value |
| :------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="java_library-name"></a>name     | Target name.                                         | none          |
| <a id="java_library-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="java_test_suite"></a>

## java_test_suite

```starlark
load("//tools/bazel:java.bzl", "java_test_suite")

java_test_suite(name, **kwargs)
```

Declare a [JUnit test suite](https://github.com/bazel-contrib/rules_jvm/blob/v0.34.0/README.md#java_test_suite).

Infers test class names using the .nz. and .lab. package prefixes and applies
the shared test defaults: small size, the test Spring profile unless set
explicitly, and Logback added to runtime dependencies.

**PARAMETERS**

| Name                                      | Description                                          | Default Value |
| :---------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="java_test_suite-name"></a>name     | Target name.                                         | none          |
| <a id="java_test_suite-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |
