# Troubleshooting

If Slate cannot start, the installed app shows why in a dialog, and `bazel run` prints why in the
terminal. Slate also writes its errors to `slate.log` in its data folder, such as
`~/Library/Application Support/Slate` on macOS, and keeps the previous run's log as `slate.old.log`.
