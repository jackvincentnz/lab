import { execFileSync } from "node:child_process";
import { readFileSync } from "node:fs";
import { join, resolve } from "node:path";
import { parseArgs } from "node:util";
import {
  collectorExclusions,
  codecovExclusions,
  compareReports,
  createReport,
  formatReport,
} from "./report.js";

try {
  const { values, positionals } = parseArgs({
    allowPositionals: true,
    options: {
      run: { type: "string" },
      baseline: { type: "string" },
      json: { type: "boolean" },
      help: { type: "boolean" },
    },
  });
  if (values.help) {
    process.stdout.write(
      "Usage: bazel run //tools/coverage_report:report -- <lcov> --run <collection command/context> [--json] [--baseline <snapshot.json>]\n",
    );
  } else {
    if (positionals.length !== 1 || !values.run) {
      throw new Error(
        "Provide one LCOV file and --run identifying targets, filters, and collection settings (see --help)",
      );
    }
    const cwd = process.env.BUILD_WORKSPACE_DIRECTORY ?? process.cwd();
    const root = execFileSync("git", ["rev-parse", "--show-toplevel"], {
      cwd,
      encoding: "utf8",
    }).trim();
    const tracked = execFileSync("git", ["ls-files", "-z"], {
      cwd: root,
      encoding: "utf8",
    })
      .split("\0")
      .filter(Boolean);
    const files = new Map(
      tracked
        .filter((path) => /\.(java|tsx?)$/.test(path) || path === "codecov.yml")
        .map((path) => [path, readFileSync(join(root, path), "utf8")]),
    );
    const inventory = JSON.parse(
      readFileSync(new URL("./inventory.json", import.meta.url), "utf8"),
    );
    const report = createReport({
      files,
      lcov: readFileSync(resolve(cwd, positionals[0]), "utf8"),
      root,
      inventory,
      collectors: collectorExclusions(files),
      uploadExclusions: codecovExclusions(files.get("codecov.yml")),
      run: values.run,
      revision: execFileSync("git", ["rev-parse", "HEAD"], {
        cwd: root,
        encoding: "utf8",
      }).trim(),
    });
    if (values.baseline) {
      report.comparison = compareReports(
        report,
        JSON.parse(readFileSync(resolve(cwd, values.baseline), "utf8")),
      );
    }
    process.stdout.write(
      values.json
        ? JSON.stringify(report, null, 2) + "\n"
        : formatReport(report),
    );
  }
} catch (error) {
  process.stderr.write(`coverage report: ${error.message}\n`);
  process.exitCode = 1;
}
