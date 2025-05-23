# Bazel Dependency Detector

Dependency detector for detecting and pushing to Github snapshots of the dependency graph in use by Bazel.

## Supported dependencies

- Lockfile from rules_jvm_external

## Usage

Run the .jar with the following environment variables set:

### Environment variables

- `REPO_ROOT_PATH`
  - Root path to search for lock file from.
  - E.g. `/<path_to_lab>/lab`
- `LOCKFILE_PATH`
  - Relative path from the root to the lockfile.
  - E.g. `maven_install.json`
- `SNAPSHOT_VERSION`
  - Not really sure, see: https://docs.github.com/en/rest/dependency-graph/dependency-submission
- `RUN_ID`
  - Unique identifer of the run that produced this snapshot.
  - E.g. `1`
- `CORRELATOR_ID`
  - Correlator provides a key used to group snapshots submitted over time. Only the "latest" submitted snapshot for a given combination of job.correlator and detector.name will be considered when calculating a repository's current dependencies.
  - E.g. `yourworkflowname_youractionname`
- `SHA`
  - Commit snapshot is produced from.
  - E.g. `e0d5f1be0f922659d2faf141f3e7c5353bab2337`
- `REF`
  - Branch snapshot is produced from.
  - E.g. `refs/heads/main`
- `ACCESS_TOKEN`
  - Personal access token for accessing the github api. Must have `repo` scope.
