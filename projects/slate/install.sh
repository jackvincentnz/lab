#!/usr/bin/env bash
set -euo pipefail

source=$1
# Tests install into a temporary folder instead.
applications=${SLATE_APPLICATIONS_DIR:-/Applications}
target=$applications/Slate.app
bundle_id=lab.slate

# Copied beside the target first, so that a failed copy leaves the installed app in place and the
# swap is two renames on one volume. Named without .app, so that the copy is not another app bundle.
staging=$(mktemp -d "$applications/.Slate-install.XXXXXX")
cleanup() {
  # A swap stopped between its renames would otherwise delete the only copy of the installed app.
  if [[ ! -e "$target" && -e "$staging/previous" ]]; then
    mv "$staging/previous" "$target"
  fi
  chmod -R u+w "$staging"
  rm -rf "$staging"
}
trap cleanup EXIT
ditto "$source" "$staging/Slate"
# Bazel outputs are read-only, so the copy is too.
chmod -R u+w "$staging/Slate"

# Checked after the copy, which takes seconds, so that they hold for what the swap moves.
if [[ -e "$target" ]]; then
  installed_id=$(plutil -extract CFBundleIdentifier raw "$target/Contents/Info.plist" 2>/dev/null || true)
  if [[ "$installed_id" != "$bundle_id" ]]; then
    echo "$target is not Slate ($bundle_id), so it was left in place. Move it, then run this again." >&2
    exit 1
  fi
  executable=$(sed 's/[][\.*^$+?(){}|]/\\&/g' <<<"$target/Contents/MacOS/Slate")
  running=0
  pgrep -qf "^$executable( |$)" || running=$?
  case $running in
    0)
      echo "Slate is running from $target. Quit it, then run this again." >&2
      exit 1
      ;;
    1) ;;
    *)
      echo "Could not check whether Slate is running, so $target was left in place." >&2
      exit 1
      ;;
  esac
  mv "$target" "$staging/previous"
fi
mv "$staging/Slate" "$target"
echo "Installed $target"
