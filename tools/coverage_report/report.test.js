import assert from "node:assert/strict";
import { execFileSync, spawnSync } from "node:child_process";
import {
  mkdirSync,
  mkdtempSync,
  readFileSync,
  readdirSync,
  rmSync,
  writeFileSync,
} from "node:fs";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { test } from "node:test";
import {
  collectorExclusions,
  codecovExclusions,
  compareReports,
  createReport,
  executableEvidence,
  formatReport,
  parseLcov,
} from "./report.js";

const root = "/synthetic/repo";
const inventory = {
  include: ["**/*.java", "**/*.ts", "**/*.tsx"],
  exclude: [],
};
const shared = `export function bazelCoverage(include, exclude = []) {
  return { include, exclude: ["**/*.test.*", "**/*.d.ts", "**/__generated__/**", ...exclude] };
}`;
const config = 'bazelCoverage(["src/**/*.js"], ["src/types.js"])';
const collectors = collectorExclusions(
  new Map([
    ["tools/bazel/vitest/coverage.ts", shared],
    ["projects/demo/vite.config.ts", config],
  ]),
);

function record(path, data = "LF:0") {
  return `SF:${path}\n${data}\nend_of_record\n`;
}

function report(files, lcov, options = {}) {
  return createReport({
    files: new Map(Object.entries(files)),
    lcov,
    root,
    inventory,
    collectors,
    run: "synthetic full coverage",
    revision: "synthetic-revision",
    ...options,
  });
}

test("LCOV unions overlapping targets and preserves repository-relative source identities", () => {
  const parsed = parseLcov(
    record(`${root}/src/./a.ts`, "DA:1,0\nLF:1\nLH:0") +
      record("src/a.ts", "DA:1,4,checksum\nDA:2,0\nLF:2\nLH:1") +
      record("src/a.ts"),
    root,
  );
  assert.deepEqual(
    [...parsed],
    [
      [
        "src/a.ts",
        new Map([
          [1, true],
          [2, false],
        ]),
      ],
    ],
  );
});

test("malformed and incomplete LCOV cannot silently improve completeness", () => {
  for (const input of [
    "",
    "SF:a.ts\nLF:0",
    "DA:1,0\n",
    "end_of_record\n",
    record("a.ts", "LF:1"),
    record("a.ts", "DA:1,0\nLF:1\nLH:1"),
    record("a.ts", "DA:0,0"),
    record("a.ts", "DA:1,-1"),
    record("a.ts", "DA:1,0\nDA:1,1"),
    record("a.ts", "LF:0\nLF:0"),
    record("a.ts", "LF:invalid"),
    record("../outside.ts"),
    record("/outside/a.ts"),
    record(""),
  ])
    assert.throws(() => parseLcov(input, root), Error, input);
});

test("LF:0 and absent records do not measure executable TS or Java", () => {
  const result = report(
    {
      "src/hit.ts": "export const value = 3;\n",
      "src/zero.tsx": "export const View = () => <div />;",
      "src/absent.ts": "export function work() { return 1; }",
      "src/Work.java": "class Work { int run() { return 1; } }",
    },
    record("src/hit.ts", "DA:1,2\nLF:1\nLH:1") +
      record("src/zero.tsx") +
      record("src/Work.java"),
  );
  assert.equal(result.summary.measured, 1);
  assert.equal(result.summary["omitted-executable"], 3);
  assert.equal(result.summary.measuredLines, 1);
  assert.equal(
    result.sources.find(({ path }) => path === "src/zero.tsx").lcov,
    "empty-record",
  );
  assert.equal(
    result.sources.find(({ path }) => path === "src/absent.ts").lcov,
    "absent",
  );
});

test("zero-hit DA records still establish measurement", () => {
  const result = report(
    { "src/a.ts": "export const a = 1;" },
    record("src/a.ts", "DA:1,0\nLF:1\nLH:0"),
  );
  assert.equal(result.summary.measured, 1);
  assert.equal(result.summary.coveredLines, 0);
  assert.equal(result.summary.measuredLines, 1);
});

test("type-only TS and TSX are non-executable independently of empty baselines", () => {
  for (const path of ["src/types.ts", "src/types.tsx"]) {
    assert.equal(
      executableEvidence(
        path,
        "import type { X } from './x'; export interface Y { x: X } export type Z = Y;",
      ).kind,
      "none",
    );
  }
  assert.equal(
    executableEvidence("src/empty.ts", "// nothing here").kind,
    "none",
  );
  assert.equal(
    executableEvidence("src/types.ts", "type ID = string; export { type ID };")
      .kind,
    "none",
  );
  assert.equal(
    executableEvidence("src/a.d.ts", "declare const value: number;").kind,
    "none",
  );
  const result = report(
    { "src/types.ts": "export type ID = string;" },
    record("src/types.ts"),
  );
  assert.equal(result.summary["non-executable"], 1);
});

test("TS runtime declarations, imports, decorators, and JSX remain executable candidates", () => {
  for (const text of [
    "export const count = 0;",
    "export function work() {}",
    "export class Empty {}",
    "import './side-effects';",
    "export { work } from './other';",
    "enum State { Active }",
    "export const View = () => <span />;",
    "function decorate(value) { return value; } @decorate class Example {}",
  ])
    assert.equal(executableEvidence("src/a.tsx", text).kind, "runtime", text);
  assert.equal(
    executableEvidence("src/broken.ts", "export function {").kind,
    "unknown",
  );
});

test("Java implicit code and declarations remain unresolved without bytecode evidence", () => {
  for (const text of [
    "class Empty {}",
    "record Value(int x) {}",
    "interface Named { String name(); }",
    "enum State { ACTIVE }",
    "class Value { static final int X = 1; }",
    'class Text { String value = "return new throw"; }',
    "class Empty {} /* return new */ // throw\n",
    'class Text { String value = """\nreturn new\n"""; }',
    "class X { \\u0072eturn; }",
  ])
    assert.equal(
      executableEvidence("src/Example.java", text).kind,
      "unknown",
      text,
    );
  assert.equal(
    executableEvidence(
      "src/package-info.java",
      "/* docs */ package example; // docs",
    ).kind,
    "none",
  );
  assert.equal(
    executableEvidence("src/Empty.java", "// only a comment").kind,
    "none",
  );
});

test("collector exclusions are read from shared and per-app literal configuration", () => {
  const result = report(
    {
      "projects/demo/src/View.test.tsx": "export const View = () => <div />;",
      "projects/demo/src/types.ts": "export type ID = string;",
      "projects/demo/src/env.d.ts": "declare const name: string;",
      "projects/demo/src/__generated__/client.ts": "export const client = {};",
      "projects/demo/src/work.ts": "export const work = 1;",
    },
    record("projects/demo/src/work.ts"),
  );
  assert.equal(result.summary.excluded, 4);
  assert.equal(result.summary["omitted-executable"], 1);
  assert.equal(
    result.sources.find(({ path }) => path.endsWith("types.ts")).exclusion
      .rules[0].source,
    "projects/demo/vite.config.ts",
  );
  assert.equal(
    result.sources.find(({ path }) => path.endsWith("View.test.tsx")).exclusion
      .rules[0].source,
    "tools/bazel/vitest/coverage.ts",
  );
});

test("a source included by another collector is not globally excluded", () => {
  const additional = collectorExclusions(
    new Map([
      ["tools/bazel/vitest/coverage.ts", shared],
      ["projects/demo/vite.config.ts", config],
      [
        "tools/bazel/vitest/vite.config.ts",
        'bazelCoverage(["projects/**/src/**/*.js"])',
      ],
    ]),
  );
  const result = report(
    { "projects/demo/src/types.ts": "export const runtime = 1;" },
    record("projects/demo/src/types.ts"),
    { collectors: additional },
  );
  assert.equal(result.summary["omitted-executable"], 1);
});

test("dynamic coverage configuration requires adapting the report instead of guessing", () => {
  for (const text of [
    "bazelCoverage(includes)",
    'bazelCoverage(["src/**/*.js"], exclusions)',
    "bazelCoverage([...includes])",
  ]) {
    assert.throws(
      () =>
        collectorExclusions(
          new Map([
            ["tools/bazel/vitest/coverage.ts", shared],
            ["projects/demo/vite.config.ts", text],
          ]),
        ),
      /literal/,
    );
  }
});

test("aliased collector imports retain exclusions and unsupported calls remain visible", () => {
  const configs = new Map([
    ["tools/bazel/vitest/coverage.ts", shared],
    [
      "projects/demo/vite.config.ts",
      'import { bazelCoverage as collect } from "./coverage"; collect(["src/**/*.js"], ["src/types.js"]);',
    ],
  ]);
  assert.equal(
    collectorExclusions(configs)[0].exclude.at(-1).pattern,
    "src/types.js",
  );
  configs.set(
    "projects/demo/vite.config.ts",
    'import { bazelCoverage as collect } from "./coverage"; const wrapper = collect; wrapper(["src/**/*.js"]);',
  );
  assert.throws(
    () => collectorExclusions(configs),
    /Cannot find imported coverage collector call/,
  );
});

test("inventory exclusions keep their reason and observed LCOV data", () => {
  const result = report(
    { "src/test/Helper.java": "class Helper { int value() { return 1; } }" },
    record("src/test/Helper.java", "DA:1,1\nLF:1"),
    {
      inventory: {
        ...inventory,
        exclude: [
          {
            pattern: "**/src/test/**",
            reason: "Test helper",
            source: "synthetic",
          },
        ],
      },
    },
  );
  assert.equal(result.summary.excluded, 1);
  assert.equal(result.summary.measuredLines, 0);
  assert.equal(result.sources[0].measuredLines, 1);
  assert.equal(result.sources[0].exclusion.reason, "Test helper");
});

test("Codecov upload exclusions retain native measurement evidence and enter comparison scope", () => {
  const files = {
    "learn/demo/src/main/java/Demo.java":
      "class Demo { int value() { return 1; } }",
  };
  const lcov = record("learn/demo/src/main/java/Demo.java", "DA:1,1\nLF:1");
  const before = report(files, lcov);
  const after = report(files, lcov, {
    uploadExclusions: codecovExclusions('ignore:\n  - "learn/**"\n'),
  });
  assert.equal(after.summary.excluded, 1);
  assert.equal(after.summary.measuredLines, 0);
  assert.equal(after.sources[0].measuredLines, 1);
  assert.equal(after.sources[0].exclusion.kind, "upload");
  assert.equal(after.sources[0].exclusion.source, "codecov.yml");
  assert.throws(() => compareReports(after, before), /not comparable/);
});

test("Codecov exclusions require literal glob lists and do not read unrelated nested ignore keys", () => {
  assert.deepEqual(codecovExclusions(undefined), []);
  assert.deepEqual(codecovExclusions("coverage:\n  ignore: nested\n"), []);
  for (const text of [
    "ignore: dynamic\n",
    "ignore:\n  - {dynamic: true}\n",
    "ignore: &rules\n  - learn/**\n",
    "ignore:\n  - *rules\n",
    "<<: *defaults\n",
    "ignore:\n  - learn/**\nignore:\n  - libs/**\n",
  ]) {
    assert.throws(() => codecovExclusions(text));
  }
});

test("unknown and out-of-inventory sources remain visible", () => {
  const result = report(
    { "src/Empty.java": "class Empty {}" },
    record("src/Empty.java") + record("generated/output.js", "DA:1,1\nLF:1"),
  );
  assert.equal(result.summary.unresolved, 1);
  assert.deepEqual(result.unexpected, [
    { path: "generated/output.js", measuredLines: 1 },
  ]);
  assert.match(formatReport(result), /unresolved\tsrc\/Empty.java/);
  assert.match(formatReport(result), /outside-inventory\tgenerated\/output.js/);
});

test("stale line numbers fail with the affected repository-relative source", () => {
  assert.throws(
    () =>
      report(
        { "src/a.ts": "const a = 1;" },
        record("src/a.ts", "DA:3,1\nLF:1"),
      ),
    /source length: src\/a.ts/,
  );
});

test("comparable snapshots show transitions, additions, removals and measured line deltas", () => {
  const before = report(
    { "src/a.ts": "const a = 1;", "src/removed.ts": "const b = 1;" },
    record("src/a.ts"),
  );
  const after = report(
    { "src/a.ts": "const a = 2;", "src/added.ts": "type ID = string;" },
    record("src/a.ts", "DA:1,1\nLF:1"),
  );
  const comparison = compareReports(after, JSON.parse(JSON.stringify(before)));
  assert.deepEqual(comparison.transitions, [
    { path: "src/a.ts", from: "omitted-executable", to: "measured" },
  ]);
  assert.deepEqual(comparison.added, ["src/added.ts"]);
  assert.deepEqual(comparison.removed, ["src/removed.ts"]);
  assert.equal(comparison.delta.measuredLines, 1);
  assert.equal(comparison.delta["omitted-executable"], -2);
  assert.equal(comparison.inventoryChanged, true);
  assert.equal(compareReports(before, before).inventoryChanged, false);
});

test("snapshots with different collection contexts or reporting scope are incomparable", () => {
  const current = report({ "src/a.ts": "const a = 1;" }, record("src/a.ts"));
  for (const baseline of [
    { ...current, schemaVersion: 0 },
    { ...current, context: { ...current.context, run: "focused test" } },
    {
      ...current,
      context: { ...current.context, classifier: "different source analysis" },
    },
    {
      ...current,
      context: { ...current.context, scopeFingerprint: "different exclusions" },
    },
  ])
    assert.throws(() => compareReports(current, baseline), /not comparable/);
});

test("coverage counts and report gaps never turn into a coverage threshold", () => {
  const result = report({ "src/a.ts": "const a = 1;" }, record("src/a.ts"));
  assert.equal(result.summary["omitted-executable"], 1);
  assert.match(formatReport(result), /Omitted line counts are unknown/);
  assert.throws(
    () =>
      report({ "src/a.ts": "const a = 1;" }, record("src/a.ts"), { run: " " }),
    /run label/,
  );
});

test("shared library reporting scope cannot mask an application's explicit exclusion", () => {
  const additional = collectorExclusions(
    new Map([
      ["tools/bazel/vitest/coverage.ts", shared],
      ["projects/demo/vite.config.ts", config],
      [
        "tools/bazel/vitest/vite.config.ts",
        'bazelCoverage(["projects/**/src/**/*.js", "libs/**/*.js"])',
      ],
    ]),
  );
  const result = report(
    { "projects/demo/src/types.ts": "export type ID = string;" },
    record("projects/demo/src/types.ts"),
    {
      collectors: additional,
      inventory: {
        ...inventory,
        collectorScopes: { "tools/bazel/vitest/vite.config.ts": ["libs/**"] },
      },
    },
  );
  assert.equal(result.summary.excluded, 1);
});

test("CLI inventories Git sources, emits comparable JSON, and leaves files untouched", (t) => {
  const fixture = mkdtempSync(
    join(process.env.TEST_TMPDIR, "coverage-report-"),
  );
  t.after(() => rmSync(fixture, { recursive: true, force: true }));
  const synthetic = {
    "tools/bazel/vitest/coverage.ts": shared,
    "projects/demo/vite.config.ts": config,
    "projects/demo/src/work.ts": "export const work = 1;",
    "projects/demo/src/types.ts": "export type ID = string;",
    "projects/demo/src/Absent.java":
      "class Absent { int work() { return 1; } }",
  };
  for (const [path, content] of Object.entries(synthetic)) {
    mkdirSync(join(fixture, path, ".."), { recursive: true });
    writeFileSync(join(fixture, path), content);
  }
  const git = (args) =>
    execFileSync("git", args, { cwd: fixture, encoding: "utf8" });
  git(["-c", "init.defaultBranch=main", "init", "--quiet"]);
  git(["add", "."]);
  git([
    "-c",
    "user.name=Synthetic",
    "-c",
    "user.email=synthetic@example.invalid",
    "-c",
    "commit.gpgsign=false",
    "commit",
    "--quiet",
    "-m",
    "test: synthetic sources",
  ]);
  const lcov = record("projects/demo/src/work.ts", "DA:1,0\nLF:1\nLH:0");
  writeFileSync(join(fixture, "lcov.dat"), lcov);
  writeFileSync(
    join(fixture, "projects/demo/src/untracked.ts"),
    "export const untracked = 1;",
  );
  const command = (args) =>
    spawnSync(
      process.execPath,
      [fileURLToPath(new URL("./cli.js", import.meta.url)), ...args],
      {
        cwd: join(fixture, "projects/demo"),
        env: { ...process.env, BUILD_WORKSPACE_DIRECTORY: fixture },
        encoding: "utf8",
      },
    );
  const args = ["lcov.dat", "--run", "synthetic full coverage", "--json"];
  const first = command(args);
  assert.equal(first.status, 0, first.stderr);
  const baseline = JSON.parse(first.stdout);
  assert.equal(baseline.summary.measured, 1);
  assert.equal(baseline.summary["omitted-executable"], 1);
  assert.equal(baseline.summary.excluded, 3);
  assert.equal(
    baseline.sources.some(({ path }) => path.endsWith("untracked.ts")),
    false,
  );
  assert.equal(baseline.summary.coveredLines, 0);
  writeFileSync(join(fixture, "baseline.json"), first.stdout);
  const filenames = readdirSync(fixture);
  const status = git(["status", "--porcelain"]);
  const next = command([...args, "--baseline", "baseline.json"]);
  assert.equal(next.status, 0, next.stderr);
  assert.equal(JSON.parse(next.stdout).comparison.delta.measured, 0);
  assert.deepEqual(readdirSync(fixture), filenames);
  assert.equal(git(["status", "--porcelain"]), status);
  assert.equal(readFileSync(join(fixture, "lcov.dat"), "utf8"), lcov);
  const text = command(args.filter((arg) => arg !== "--json"));
  assert.equal(text.status, 0, text.stderr);
  assert.match(
    text.stdout,
    /omitted-executable\tprojects\/demo\/src\/Absent.java/,
  );
  const mismatch = command([
    "lcov.dat",
    "--run",
    "other collection",
    "--baseline",
    "baseline.json",
  ]);
  assert.equal(mismatch.status, 1);
  assert.match(mismatch.stderr, /not comparable/);
  assert.equal(mismatch.stdout, "");
  const noRun = command(["lcov.dat"]);
  assert.equal(noRun.status, 1);
  assert.match(noRun.stderr, /--run/);
  const help = command(["--help"]);
  assert.equal(help.status, 0);
  assert.match(help.stdout, /Usage:/);
});
