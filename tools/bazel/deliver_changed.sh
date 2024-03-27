#!/usr/bin/env bash

# Find all delivery targets
targets=$(bazel query 'kind("push_if_not_exists", //...)' --output label)

# Learn target count for parallel pushes
targets_count=$(echo "$targets" | wc -l)

# Parallel run all delivery targets in release mode (with version stamping enabled)
# xargs drops stdout entries in parallel mode. Remove -P if debugging. See: https://stackoverflow.com/a/32463179
echo "$targets" | xargs -P "$targets_count" -n 1 bazel run --config=release
