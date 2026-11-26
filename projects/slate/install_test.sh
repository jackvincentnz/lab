#!/usr/bin/env bash
set -euo pipefail

install=$PWD/projects/slate/install.sh
failures=0

make_app() {
  local app=$1 id=$2 marker=$3
  mkdir -p "$app/Contents/MacOS"
  plutil -create xml1 "$app/Contents/Info.plist"
  plutil -insert CFBundleIdentifier -string "$id" "$app/Contents/Info.plist"
  # macOS kills a copy of a system binary, but a link runs it under the bundle's path.
  ln -s /bin/sleep "$app/Contents/MacOS/Slate"
  echo "$marker" >"$app/Contents/marker"
  # Like a Bazel output.
  chmod -R a-w "$app"
}

# Runs one test in a fresh folder that stands in for /Applications. Outside a condition, so that
# `set -e` fails the test at its first failing command.
run() {
  local name=$1
  local dir status
  # A regex character in the path must not defeat the running-app check.
  dir=$(mktemp -d "$TEST_TMPDIR/$name+1.XXXXXX")
  set +e
  (
    set -e
    cd "$dir"
    mkdir Applications
    "$name"
  )
  status=$?
  set -e
  if [[ $status -eq 0 ]]; then
    echo "PASS $name"
  else
    echo "FAIL $name"
    failures=$((failures + 1))
  fi
}

fails() {
  if "$@" 2>/dev/null; then
    return 1
  fi
}

install_app() {
  SLATE_APPLICATIONS_DIR=$PWD/Applications "$install" "$1"
}

marker() {
  cat Applications/Slate.app/Contents/marker
}

no_staging_left() {
  [[ -z "$(find Applications -mindepth 1 -maxdepth 1 -name '.*')" ]]
}

installs_into_an_empty_folder() {
  make_app new.app lab.slate new

  install_app new.app

  [[ "$(marker)" == new ]] && [[ -w Applications/Slate.app/Contents/marker ]] && no_staging_left
}

replaces_an_installed_slate() {
  make_app old.app lab.slate old
  install_app old.app
  make_app new.app lab.slate new

  install_app new.app

  [[ "$(marker)" == new ]] && no_staging_left
}

leaves_another_app_in_place() {
  make_app other.app example.other other
  ditto other.app Applications/Slate.app
  # Installed apps are writable, so only the identity check can stop the install.
  chmod -R u+w Applications/Slate.app
  make_app new.app lab.slate new

  fails install_app new.app

  [[ "$(marker)" == other ]] && no_staging_left
}

leaves_an_app_without_an_identifier_in_place() {
  mkdir -p Applications/Slate.app/Contents
  echo other >Applications/Slate.app/Contents/marker
  make_app new.app lab.slate new

  fails install_app new.app

  [[ "$(marker)" == other ]] && no_staging_left
}

leaves_a_running_slate_in_place() {
  make_app old.app lab.slate old
  install_app old.app
  make_app new.app lab.slate new
  "$PWD/Applications/Slate.app/Contents/MacOS/Slate" 60 &
  trap 'kill $!' EXIT
  # The background job runs as a copy of this shell until it starts the app.
  for _ in {1..50}; do
    pgrep -qf "Contents/MacOS/Slate 60" && break
    sleep 0.1
  done

  fails install_app new.app

  [[ "$(marker)" == old ]] && no_staging_left
}

keeps_the_installed_slate_when_the_copy_fails() {
  make_app old.app lab.slate old
  install_app old.app

  fails install_app missing.app

  [[ "$(marker)" == old ]] && no_staging_left
}

keeps_the_installed_slate_when_the_swap_fails() {
  make_app old.app lab.slate old
  install_app old.app
  make_app new.app lab.slate new
  mkdir bin
  printf '#!/bin/sh\ncase "$1" in */Slate) exit 1 ;; esac\nexec /bin/mv "$@"\n' >bin/mv
  chmod +x bin/mv

  PATH=$PWD/bin:$PATH fails install_app new.app

  [[ "$(marker)" == old ]] && no_staging_left
}

leaves_slate_in_place_when_the_process_check_fails() {
  make_app old.app lab.slate old
  install_app old.app
  make_app new.app lab.slate new
  mkdir bin
  printf '#!/bin/sh\nexit 3\n' >bin/pgrep
  chmod +x bin/pgrep

  PATH=$PWD/bin:$PATH fails install_app new.app

  [[ "$(marker)" == old ]] && no_staging_left
}

run installs_into_an_empty_folder
run replaces_an_installed_slate
run leaves_another_app_in_place
run leaves_an_app_without_an_identifier_in_place
run leaves_a_running_slate_in_place
run keeps_the_installed_slate_when_the_copy_fails
run keeps_the_installed_slate_when_the_swap_fails
run leaves_slate_in_place_when_the_process_check_fails

exit "$failures"
