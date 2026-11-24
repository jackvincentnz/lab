#!/usr/bin/env bash
set -euo pipefail

target=/Applications/Slate.app

if [[ -e "$target" ]]; then
  # Bazel outputs are read-only, so an earlier install is too.
  chmod -R u+w "$target"
  rm -rf "$target"
fi
ditto "$1" "$target"
chmod -R u+w "$target"
echo "Installed $target"
