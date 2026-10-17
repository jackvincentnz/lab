<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# Java

Shared Java wrappers keep external rules and test defaults in one place.

<a id="java_binary"></a>

## java_binary

<pre>
load("//tools/bazel:java.bzl", "java_binary")

java_binary(<a href="#java_binary-name">name</a>, <a href="#java_binary-kwargs">**kwargs</a>)
</pre>

Declare a [java_binary](https://bazel.build/reference/be/java#java_binary).

**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="java_binary-name"></a>name |  Target name.   |  none |
| <a id="java_binary-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


<a id="java_junit5_test"></a>

## java_junit5_test

<pre>
load("//tools/bazel:java.bzl", "java_junit5_test")

java_junit5_test(<a href="#java_junit5_test-name">name</a>, <a href="#java_junit5_test-kwargs">**kwargs</a>)
</pre>

Declare a [JUnit 5 test](https://github.com/bazel-contrib/rules_jvm/blob/main/README.md#java_junit5_test).

Defaults to small size and the test Spring profile unless set explicitly,
and adds Logback to runtime dependencies.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="java_junit5_test-name"></a>name |  Target name.   |  none |
| <a id="java_junit5_test-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


<a id="java_library"></a>

## java_library

<pre>
load("//tools/bazel:java.bzl", "java_library")

java_library(<a href="#java_library-name">name</a>, <a href="#java_library-kwargs">**kwargs</a>)
</pre>

Declare a [java_library](https://bazel.build/reference/be/java#java_library).

**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="java_library-name"></a>name |  Target name.   |  none |
| <a id="java_library-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


<a id="java_test_suite"></a>

## java_test_suite

<pre>
load("//tools/bazel:java.bzl", "java_test_suite")

java_test_suite(<a href="#java_test_suite-name">name</a>, <a href="#java_test_suite-kwargs">**kwargs</a>)
</pre>

Declare a [JUnit test suite](https://github.com/bazel-contrib/rules_jvm/blob/main/README.md#java_test_suite).

Infers test class names using the `.nz.` and `.lab.` package prefixes and applies
the shared test defaults: small size, the test Spring profile unless set
explicitly, and Logback added to runtime dependencies.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="java_test_suite-name"></a>name |  Target name.   |  none |
| <a id="java_test_suite-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


