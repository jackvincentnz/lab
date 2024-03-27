#!/usr/bin/env bash
set -o pipefail -o errexit -o nounset

readonly CRANE="{{crane_path}}"
readonly YQ="{{yq_path}}"
readonly IMAGE_DIR="{{image_dir}}"
readonly REPOSITORY="{{repository}}"
readonly PUSHER="{{pusher_path}}"

DIGEST=$("${YQ}" eval '.manifests[0].digest' "${IMAGE_DIR}/index.json")

if "${CRANE}" digest "${REPOSITORY}@${DIGEST}"; then
  echo "${REPOSITORY}@${DIGEST} exists, not pushing new tags"
  exit 0;
else
  echo "${REPOSITORY}@${DIGEST} does not exist, pushing and tagging"
  "${PUSHER}"
fi
