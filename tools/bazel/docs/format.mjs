import { readFile } from "node:fs/promises";
import process from "node:process";
import { format } from "prettier";

// Bazel sandbox inputs are symlinks, which Prettier's CLI refuses to follow.
process.stdout.write(
  await format(await readFile(process.argv[2], "utf8"), { parser: "markdown" }),
);
