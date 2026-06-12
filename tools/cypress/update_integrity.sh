#!/usr/bin/env bash

# Generate integrity hashes for the cypress_version in MODULE.bazel by running:
#   bazel run @aspect_rules_cypress//scripts:mirror_releases -- <VERSION>
# Then write the generated platform hashes into the cypress_integrity block.

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly WORKSPACE_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly MODULE_FILE="${WORKSPACE_DIR}/MODULE.bazel"

# The package and Bazel toolchain versions are kept together by Renovate.
version="$(sed -n 's/.*cypress_version[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' "${MODULE_FILE}")"
if [[ -z "${version}" || "${version}" == *$'\n'* ]]; then
    echo "Expected exactly one cypress_version declaration in ${MODULE_FILE}" >&2
    exit 1
fi

mirror_output="$(mktemp)"
updated_module="$(mktemp)"
trap 'rm -f "${mirror_output}" "${updated_module}"' EXIT

echo "Generating Cypress ${version} integrity values..."
(
    cd "${WORKSPACE_DIR}"
    bazel run @aspect_rules_cypress//scripts:mirror_releases -- "${version}"
) >"${mirror_output}"

integrity_block="$(grep -E '^[[:space:]]*"[^"]+": "[0-9a-f]{64}",$' "${mirror_output}" || true)"

if [[ -z "${integrity_block}" ]]; then
    echo "Missing or invalid integrity values from mirror_releases" >&2
    exit 1
fi

INTEGRITY_BLOCK="${integrity_block}" perl -0pe '
    BEGIN { $replacement = $ENV{"INTEGRITY_BLOCK"} }
    $count = s/(?<=cypress_integrity = \{\n).*?(?=    \},\n    cypress_version)/$replacement\n/s;
    END { exit 1 unless $count == 1 }
' "${MODULE_FILE}" >"${updated_module}" || {
    echo "Failed to replace the cypress_integrity block in ${MODULE_FILE}" >&2
    exit 1
}

if cmp -s "${MODULE_FILE}" "${updated_module}"; then
    echo "Cypress ${version} integrity values are already current."
    exit 0
fi

mv "${updated_module}" "${MODULE_FILE}"
echo "Updated Cypress ${version} integrity values in ${MODULE_FILE}."
