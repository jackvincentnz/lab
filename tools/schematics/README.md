# Getting Started With Schematics

This package is a basic Schematic implementation that serves as a starting point to create schematics for use in this repo.

## Getting started

To setup locally, install `@angular-devkit/schematics-cli` globally and use the `schematics` command line tool. The CLI tool acts the same as the `generate` command of the Angular CLI, but also has a debug mode.

Check the documentation with:

```bash
$ schematics --help
```

## Adding a schematic

1. Navigate to the schematics directory.

   ```bash
   $ cd tools/schematics
   ```

2. Create boilerplate schematic.

   ```
   $ schematics blank --name=<schematic_name>
   ```

3. Move files to correct location.

   ```bash
   $ mv src/* .
   ```

4. Remove generated `tools/schematics/src`.

   ```bash
   $ rm -rf src
   ```

5. Rename `<schematic_name>_spec.ts` to `<schematic_name>.spec.ts`.

   ```bash
   $ mv <schematic_name>_spec.ts <schematic_name>.spec.ts
   ```

6. Verify tests are passing.

   ```bash
   $ bazel test //tools/schematics/...
   ```

7. Run a schematic from the collection.

   ```bash
   $ ng g ./dist/bin/tools/schematics:hello-world
   ```
