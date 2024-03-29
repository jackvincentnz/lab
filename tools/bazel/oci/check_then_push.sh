#!/usr/bin/env bash

CRANE=""
YQ=""
IMAGE_DIR=""
REPOSITORY=""
PUSHER=""

usage() {
    echo "Usage: $0 --crane_path <CRANE_PATH> --yq_path <YQ_PATH> --image_dir <IMAGE_DIR> --repository <REPOSITORY> --pusher_path <PUSHER_PATH>"
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
        --yq_path)
            YQ="$2"
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

if [[ -z $CRANE || -z $YQ || -z $IMAGE_DIR || -z $REPOSITORY || -z $PUSHER ]]; then
    echo "Error: Missing required arguments"
    usage
fi

# Extract digest from image outputs
DIGEST=$("${YQ}" eval '.manifests[0].digest' "${IMAGE_DIR}/index.json")

if "${CRANE}" digest "${REPOSITORY}@${DIGEST}"; then
  echo "${REPOSITORY}@${DIGEST} exists, not pushing new tags"
  exit 0;
else
  echo "${REPOSITORY}@${DIGEST} does not exist, pushing and tagging"
  "${PUSHER}"
fi
