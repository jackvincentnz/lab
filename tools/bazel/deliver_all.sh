#!/usr/bin/env bash

# Find all executable oci_push targets
oci_push_targets=$(bazel query 'kind("oci_push", //...)' --output label)

# Learn target count for parallel pushes
oci_push_targets_count=$(echo "$oci_push_targets" | wc -l)

# Parallel run all oci_push targets in release mode (with version stamping enabled)
# xargs drops stdout entries in parallel mode. Remove -P if debugging. See: https://stackoverflow.com/a/32463179
echo "$oci_push_targets" | xargs -P "$oci_push_targets_count" -n 1 bazel run --config=release
