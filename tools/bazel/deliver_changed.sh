#!/usr/bin/env bash

# Find all delivery targets
targets=$(bazel query 'attr("tags", "deliverable", //...)' --output label)

# Learn target count for parallel pushes
targets_count=$(echo "$targets" | wc -l)

# Parallel run all delivery targets in release mode (with version stamping enabled)
# xargs drops stdout entries in parallel mode. Remove -P if debugging. See: https://stackoverflow.com/a/32463179
echo "$targets" | xargs -P "$targets_count" -n 1 bazel \
  --bazelrc=.aspect/bazelrc/ci.bazelrc \
  --bazelrc=.github/workflows/ci.bazelrc \
  run \
  --config=release \
  --remote_header=x-buildbuddy-api-key="$BUILDBUDDY_API_KEY"
