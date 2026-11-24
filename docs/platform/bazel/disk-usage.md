# Disk usage

Bazel keeps its state outside the checkout, and most of that state belongs to
one checkout. Each worktree therefore adds an output base, which reaches about
8 GB in this repository. This page covers where the state lives, what
`.bazelrc` shares and bounds, how to override it, and how to reclaim space.

## Where Bazel keeps its state

The output user root is the folder that holds Bazel's state, apart from the
caches that `.bazelrc` places beside it.

| State                     | Holds                                                                        | Shared between checkouts | Location                                             |
| ------------------------- | ---------------------------------------------------------------------------- | ------------------------ | ---------------------------------------------------- |
| Output user root          | The install base, output bases, and repository cache.                        | Yes                      | See [operating systems](#operating-systems).         |
| Install base              | The unpacked Bazel binary, one per Bazel version.                            | Yes                      | `<root>/install/`                                    |
| Output base               | Build outputs, external repositories, and the Bazel server for one checkout. | No                       | `<root>/<hash of the checkout path>/`                |
| Repository cache          | Downloaded archives.                                                         | Yes                      | `<root>/cache/repos/v1/`                             |
| Repository contents cache | Extracted external repositories, which output bases link to.                 | Yes                      | `repo-contents/` beside the root, set by `.bazelrc`. |
| Disk cache                | Action outputs.                                                              | Yes                      | `disk-cache/` beside the root, set by `.bazelrc`.    |

Run `bazel info output_base` in a checkout to find its output base.

## Repository settings

[`.bazelrc`](../../../.bazelrc) sets these options on macOS and Linux:

| Option                                  | Value                                                   | Reason                                                                                                                         |
| --------------------------------------- | ------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `--disk_cache`                          | `disk-cache/` in the [Bazel folder](#operating-systems) | A new output base, such as a new worktree's, builds warm from the outputs of other checkouts.                                  |
| `--experimental_disk_cache_gc_max_size` | `20G`                                                   | It bounds the disk cache. Bazel deletes the least recently used entries after the server has been idle for 5 minutes.          |
| `--repo_contents_cache`                 | `repo-contents/` in the Bazel folder                    | A new worktree links to most external repositories instead of extracting them again. Bazel deletes entries unused for 14 days. |

[`ci.bazelrc`](../../../.github/workflows/ci.bazelrc) turns off the disk cache, so
that CI runners stay small.

## Override settings

Bazel reads its configuration files in this order. For the same option, a
later file overrides an earlier one.

| File                  | Applies to                            | Notes                                                                           |
| --------------------- | ------------------------------------- | ------------------------------------------------------------------------------- |
| `.bazelrc`            | Everyone who builds this repository.  | Imports `tools/preset.bazelrc` first.                                           |
| `user.bazelrc`        | One checkout.                         | `.bazelrc` imports it last. Git ignores it, so a new worktree does not have it. |
| `~/.bazelrc`          | Every Bazel workspace on the machine. | Use it for settings that every worktree needs.                                  |
| `--bazelrc` and flags | One command.                          | CI adds `ci.bazelrc` this way.                                                  |

Override an option with a line for the same command, such as `build` for
`--disk_cache` and `common` for `--repo_contents_cache`. A line for a less
specific command does not override it: a `common --disk_cache` line in
`~/.bazelrc` leaves the disk cache that `.bazelrc` sets. A plain `build` line in
`user.bazelrc` or `~/.bazelrc` does override a `build:macos` line in `.bazelrc`.

For example, these lines in `~/.bazelrc` change each setting for every checkout
on the machine:

```text
# Allow a larger disk cache.
build --experimental_disk_cache_gc_max_size=50G
# Move the disk cache to another disk.
build --disk_cache=/Volumes/Work/bazel-disk-cache
# Turn off the repository contents cache.
common --repo_contents_cache=
```

An empty value turns an option off, and `~` expands in path options. Environment
variables such as `$USER` do not expand, so write the user name in full.

If an agent sandbox limits where commands can write, let it write to the Bazel
folder for your operating system, so that Bazel can use the shared caches.

## Output user root on macOS

Bazel 8 puts the output user root in `/private/var/tmp` on macOS, apart from the
caches. Bazel 9 moves it to `~/Library/Caches/bazel/_bazel_<user>`, which is
also where `aspect gc`, `aspect output-bases`, and `aspect worktree` look for
output bases. Add this line to `~/.bazelrc`, with your user name in place of
`<user>`, so that all of Bazel's state is in one folder and `aspect` sees it:

```text
startup --output_user_root=~/Library/Caches/bazel/_bazel_<user>
```

`.bazelrc` cannot set it, because the path holds the user name. A new output
user root starts every checkout cold once. Run `bazel shutdown` in each
checkout, then delete `/private/var/tmp/_bazel_<user>`.

## Reclaim space

| Command                               | Effect                                                                                                                                                                              |
| ------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `aspect output-bases --measure-sizes` | Lists every output base with its state, workspace, branch, and size. It deletes nothing.                                                                                            |
| `aspect gc --dry-run`                 | Shows what `aspect gc` would delete: output bases whose checkout is gone, output bases unused for 30 days, and downloads unused for 30 days. It keeps `aspect worktree` pool slots. |
| `aspect worktree prune`               | Deletes free pool slots unused for 30 days, with their output bases. `aspect worktree add` also runs it.                                                                            |
| `bazel clean --expunge`               | Deletes the output base of the current checkout. Run it before you delete a worktree that you made with `git worktree add`.                                                         |

A pool slot that `aspect worktree release` freed has no checkout, but it keeps
its output base so that the next worktree starts warm. Check
`aspect worktree list --all` before you delete an output base by hand.

## Operating systems

| Setting                             | macOS                                  | Linux                          |
| ----------------------------------- | -------------------------------------- | ------------------------------ |
| Bazel folder                        | `~/Library/Caches/bazel`               | `~/.cache/bazel`               |
| Default output user root in Bazel 8 | `/private/var/tmp/_bazel_<user>`       | `~/.cache/bazel/_bazel_<user>` |
| Default output user root in Bazel 9 | `~/Library/Caches/bazel/_bazel_<user>` | `~/.cache/bazel/_bazel_<user>` |

The repository builds with Bazel 8. On other operating systems, such as Windows,
`.bazelrc` turns on neither cache. Add `build --disk_cache` and
`common --repo_contents_cache` lines to `~/.bazelrc` to use them.
