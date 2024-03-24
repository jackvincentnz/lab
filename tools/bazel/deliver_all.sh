#!/usr/bin/env bash

# Find all executable oci_push targets
oci_push_targets=$(bazel query 'kind("oci_push", //...)' --output label)

# Run all oci_push targets in release mode (with version stamping enabled)
echo "$oci_push_targets" | xargs -n 1 bazel run --config=release
