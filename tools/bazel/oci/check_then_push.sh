#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

CRANE=""
JQ=""
IMAGE_DIR=""
REPOSITORY=""
PUSHER=""

usage() {
    echo "Usage: $0 --crane_path <CRANE_PATH> --jq_path <JQ_PATH> --image_dir <IMAGE_DIR> --repository <REPOSITORY> --pusher_path <PUSHER_PATH>"
    exit 1
}

while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        --crane_path)
            CRANE="$2"
            shift
            shift
            ;;
        --jq_path)
            JQ="$2"
            shift
            shift
            ;;
        --image_dir)
            IMAGE_DIR="$2"
            shift
            shift
            ;;
        --repository)
            REPOSITORY="$2"
            shift
            shift
            ;;
        --pusher_path)
            PUSHER="$2"
            shift
            shift
            ;;
        *)
            echo "Error: Unknown option '$1'"
            usage
            ;;
    esac
done

if [[ -z $CRANE || -z $JQ || -z $IMAGE_DIR || -z $REPOSITORY || -z $PUSHER ]]; then
    echo "Error: Missing required arguments"
    usage
fi

# `oci_push` launcher scripts require runfiles env vars when invoked as subprocesses.
# `bazel run` does not provide them for this raw source-backed `sh_binary`.
if [[ -z ${RUNFILES_DIR:-} && -z ${RUNFILES_MANIFEST_FILE:-} ]]; then
    if [[ -d "${0}.runfiles" ]]; then
        export RUNFILES_DIR="${0}.runfiles"
    elif [[ -f "${0}.runfiles_manifest" ]]; then
        export RUNFILES_MANIFEST_FILE="${0}.runfiles_manifest"
    fi
fi
if [[ -z ${RUNFILES_REPO_MAPPING:-} && -f "${0}.repo_mapping" ]]; then
    export RUNFILES_REPO_MAPPING="${0}.repo_mapping"
fi

# Extract digest from image outputs
DIGEST=$("${JQ}" -r '.manifests[0].digest' "${IMAGE_DIR}/index.json")

if "${CRANE}" digest "${REPOSITORY}@${DIGEST}"; then
    echo "${REPOSITORY}@${DIGEST} exists, not pushing new tags"
    exit 0
else
    echo "${REPOSITORY}@${DIGEST} does not exist, pushing and tagging"
    "${PUSHER}"
fi
