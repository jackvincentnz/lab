# Disk usage

Bazel keeps its state outside the checkout, and most of it belongs to one
checkout. Each worktree therefore adds an output base of about 8 GB. Bazel's
[output directory layout](https://bazel.build/remote/output-directories)
describes each folder in detail.

## Layout

This is the layout on macOS with the output user root moved as
[below](#output-user-root-on-macos), and on Linux by default:

```text
~/Library/Caches/bazel/              Bazel folder; ~/.cache/bazel on Linux
├── _bazel_<user>/                   output user root
│   ├── install/                     Bazel itself, one folder per version
│   ├── cache/repos/v1/              downloaded archives, shared
│   └── <hash of the checkout path>/ output base, one per checkout
│       ├── execroot/                build outputs, which dist/ links to
│       └── external/                external repositories, links into repo-contents/
├── disk-cache/                      action outputs, shared, up to 20 GB
└── repo-contents/                   extracted external repositories, shared
```

[`.bazelrc`](../../../.bazelrc) sets `disk-cache/` and `repo-contents/` on macOS
and Linux, so that a new worktree builds from cached outputs and links to most
external repositories instead of extracting them again. Bazel deletes the least
recently used disk cache entries above 20 GB, and repository contents unused for
14 days. [`ci.bazelrc`](../../../.github/workflows/ci.bazelrc) turns off the disk
cache, so that CI runners stay small. On other operating systems, `.bazelrc`
sets neither cache.

## Output user root on macOS

Bazel 8 puts the output user root in `/private/var/tmp/_bazel_<user>` on macOS.
Bazel 9 moves it into the Bazel folder, as in the layout above, which is also
where `aspect` looks for output bases. To move it now, add this line to
`~/.bazelrc` with your user name:

```text
startup --output_user_root=~/Library/Caches/bazel/_bazel_<user>
```

Every checkout then starts cold once. Run `bazel shutdown` in each checkout,
then delete `/private/var/tmp/_bazel_<user>`.

## Override settings

Put personal settings in `~/.bazelrc`, which overrides `.bazelrc` for every
checkout on the machine. A checkout's `user.bazelrc` also overrides `.bazelrc`,
but a new worktree does not have one, and `~/.bazelrc` overrides it.

Use the same command as the line you override: `build` for the disk cache
options and `common` for `--repo_contents_cache`. A `common` line does not
override a `build` line. For example:

```text
# Allow a larger disk cache.
build --experimental_disk_cache_gc_max_size=50G
# Move the disk cache to another disk.
build --disk_cache=/Volumes/Work/bazel-disk-cache
# Turn off the repository contents cache.
common --repo_contents_cache=
```

`~` expands in paths, but `$USER` does not. If an agent sandbox limits where
commands can write, let it write to the Bazel folder.

## Reclaim space

- `aspect output-bases --measure-sizes` lists every output base with its
  checkout, branch, and size.
- `aspect gc` deletes output bases whose checkout is gone or that have not been
  used for 30 days, and keeps `aspect worktree` pool slots. Add `--dry-run` to
  see what it would delete.
- `aspect worktree prune` deletes free pool slots that have not been used for
  30 days.
- `bazel clean --expunge` deletes the current checkout's output base. Run it
  before you delete a worktree that you made with `git worktree add`.

A free pool slot has no checkout but keeps its output base, so that the next
worktree starts warm. Check `aspect worktree list --all` before you delete an
output base by hand.
