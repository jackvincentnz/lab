# Troubleshooting

If Slate cannot start, the installed app shows why in a dialog, and `bazel run` prints why in the
terminal. The desktop app also writes its errors and the service's output to `slate.log` in its data
folder, `~/Library/Application Support/Slate` on macOS or `Slate (dev)` in dev mode, and keeps the
previous run's log as `slate.old.log`.
