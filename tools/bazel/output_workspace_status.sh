#!/usr/bin/env bash

# Outputs mono version.
# Version is in the format [year].[week].[# commits so far that week]-[git SHA]
# See: https://blog.aspect.dev/versioning-releases-from-a-monorepo#heading-automated
# The version is valid semver for a pre-release, see: https://semver.org/#spec-item-9
version=$(git describe --tags --long --match="[0-9][0-9][0-9][0-9].[0-9][0-9]" | sed -e 's/-/./;s/-g/-/')
echo "STABLE_VERSION ${version}"
