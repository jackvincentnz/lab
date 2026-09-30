# README and documentation guidelines

Start with a brief description of what the app, service, or library does, then
list the commands needed to build, run, and test it. State where to run commands
and include essential details such as development URLs or prerequisites. Keep
READMEs for similar components consistent in structure and command presentation.

Place shared setup and workflows at the directory level that owns them. For
example, document a multi-service environment and its end-to-end tests in the
parent project README, then link to it from each app. Link to shared tooling or
contribution guides instead of repeating their implementation details.

## Documentation

- Keep READMEs to a description, prerequisites, and the commands to build, run,
  and test. Put explanations and operating procedures in `projects/<name>/docs/`
  and link to them from the README.
- Put architecture decision records in [`docs/adr`](../adr/README.md). Record
  the context, decision, alternatives, and consequences, without status
  updates, test counts, or how the record was drafted.
- Describe the current state only. Do not add change history, completed items,
  test results, or descriptions of what a UI looks like.
- Keep docs short. Link to the document that owns a topic instead of repeating
  its setup or tool details.
- Write commands that work from any clone. Use repository-relative paths or
  `$PWD`, never paths in a user's home directory.
- Use standard tools such as `openssl` for operational steps. If no standard
  tool produces a format, change the format the code accepts instead of
  documenting a script.
- Do not list new projects in the root README.

## Comments

Code, configuration, and BUILD comments explain why something is the way it is.
Do not reference future work or issue numbers, such as "once #123 lands";
sequencing belongs in the tracking issue.
