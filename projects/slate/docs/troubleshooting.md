# Troubleshooting

If Slate cannot start, the installed app shows why in a dialog. A build started with `bazel run`
prints why in the terminal.

Once it holds its data folder, the desktop app writes errors and the service's output to `slate.log`
in its [data folder](../README.md#getting-started). It keeps the previous run's log as
`slate.old.log`.
