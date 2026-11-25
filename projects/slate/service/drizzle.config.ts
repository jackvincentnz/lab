import { join } from "node:path";
import { defineConfig } from "drizzle-kit";

export default defineConfig({
  dialect: "sqlite",
  schema: "src/schema.js",
  // `bazel run` starts in the runfiles, so migrations are written to the source tree instead.
  out: join(
    process.env["BUILD_WORKSPACE_DIRECTORY"] ?? "",
    "projects/slate/service/migrations",
  ),
});
