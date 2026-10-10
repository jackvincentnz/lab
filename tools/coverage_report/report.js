import { createHash } from "node:crypto";
import { dirname, isAbsolute, matchesGlob, posix, relative } from "node:path";
import { version as parserVersion } from "prettier";
import { parsers } from "prettier/plugins/typescript";
import { parsers as yamlParsers } from "prettier/plugins/yaml";

const schemaVersion = 1;
const statuses = [
  "measured",
  "omitted-executable",
  "excluded",
  "non-executable",
  "unresolved",
];

export function digest(value) {
  return createHash("sha256").update(JSON.stringify(value)).digest("hex");
}

function repositoryPath(path, root) {
  const normalized = posix.normalize(
    (isAbsolute(path) ? relative(root, path) : path).replaceAll("\\", "/"),
  );
  if (normalized === ".." || normalized.startsWith("../")) {
    throw new Error("LCOV source path is outside the repository");
  }
  return normalized;
}

/** Merge line identities, since overlapping test targets can report one file. */
export function parseLcov(input, root) {
  const records = new Map();
  let current;
  for (const line of input.split(/\r?\n/)) {
    if (line.startsWith("SF:")) {
      if (current) throw new Error("LCOV record is missing end_of_record");
      const path = repositoryPath(line.slice(3), root);
      if (!path || path === ".") throw new Error("LCOV has an empty SF path");
      current = { path, lines: new Map() };
    } else if (/^(DA|LF|LH):/.test(line)) {
      if (!current) throw new Error("LCOV line data has no SF record");
      if (line.startsWith("DA:")) {
        const match = /^DA:([1-9]\d*),(\d+)(?:,[^,]+)?$/.exec(line);
        if (!match) throw new Error("Invalid LCOV DA record");
        const number = Number(match[1]);
        const hits = Number(match[2]);
        if (!Number.isSafeInteger(number) || !Number.isSafeInteger(hits)) {
          throw new Error("LCOV DA value exceeds the safe integer range");
        }
        if (current.lines.has(number))
          throw new Error("Duplicate LCOV DA line");
        current.lines.set(number, hits > 0);
      } else {
        if (!/^(LF|LH):\d+$/.test(line))
          throw new Error("Invalid LCOV line total");
        const key = line.slice(0, 2);
        if (current[key] !== undefined)
          throw new Error("Duplicate LCOV line total");
        current[key] = Number(line.slice(3));
      }
    } else if (line === "end_of_record") {
      if (!current) throw new Error("LCOV end_of_record has no SF record");
      const covered = [...current.lines.values()].filter(Boolean).length;
      if (
        (current.LF !== undefined && current.LF !== current.lines.size) ||
        (current.LH !== undefined && current.LH !== covered)
      ) {
        throw new Error(
          `LCOV line totals disagree with DA records: ${current.path}`,
        );
      }
      const merged = records.get(current.path) ?? new Map();
      for (const [number, hit] of current.lines) {
        merged.set(number, Boolean(merged.get(number)) || hit);
      }
      records.set(current.path, merged);
      current = undefined;
    }
  }
  if (current) throw new Error("LCOV record is missing end_of_record");
  if (!records.size) throw new Error("LCOV contains no source records");
  return records;
}

function sourceFile(path, source) {
  return parsers.typescript.parse(source, { filepath: path });
}

function walk(node, visit) {
  if (!node || typeof node !== "object") return;
  if (typeof node.type === "string") visit(node);
  for (const value of Object.values(node)) {
    if (Array.isArray(value)) value.forEach((child) => walk(child, visit));
    else if (value && typeof value === "object") walk(value, visit);
  }
}

function strings(node, path) {
  if (!node || node.type !== "ArrayExpression") {
    throw new Error(`Coverage patterns must be literal arrays: ${path}`);
  }
  return node.elements.map((element) => {
    if (element?.type !== "Literal" || typeof element.value !== "string") {
      throw new Error(`Coverage pattern must be a string literal: ${path}`);
    }
    return element.value;
  });
}

/** Upload exclusions do not remove native LCOV records. Keep their provenance. */
export function codecovExclusions(text) {
  if (text === undefined) return [];
  const documents = yamlParsers.yaml.parse(text).children;
  if (documents.length !== 1)
    throw new Error("Expected one Codecov YAML document");
  const mapping = documents[0].children.find(
    (node) => node.type === "documentBody",
  )?.children[0];
  if (mapping?.type !== "mapping")
    throw new Error("Expected Codecov YAML mapping");
  const items = mapping.children.filter(
    (item) => item.children[0].children[0]?.value === "ignore",
  );
  if (
    mapping.children.some(
      (item) => item.children[0].children[0]?.value === "<<",
    ) ||
    items.length > 1
  ) {
    throw new Error("Unsupported Codecov YAML merge or duplicate ignore key");
  }
  if (!items.length) return [];
  const sequence = items[0].children[1].children[0];
  if (sequence?.type !== "sequence" || sequence.anchor || sequence.tag) {
    throw new Error(
      "Codecov ignore must be a literal sequence of glob strings",
    );
  }
  return sequence.children.map((item) => {
    const scalar = item.children[0];
    if (
      !scalar ||
      !["plain", "quoteSingle", "quoteDouble"].includes(scalar.type) ||
      scalar.anchor ||
      scalar.tag ||
      !scalar.value ||
      scalar.value.includes("\\")
    ) {
      throw new Error("Codecov ignore must contain literal glob strings");
    }
    return {
      pattern: scalar.value,
      source: "codecov.yml",
      kind: "upload",
      reason: "Ignored by Codecov upload; native LCOV records are retained.",
    };
  });
}

/** Read collector configuration without executing Vite plugins or changing it. */
export function collectorExclusions(files) {
  const sharedPath = "tools/bazel/vitest/coverage.ts";
  const shared = sourceFile(sharedPath, files.get(sharedPath));
  let defaults;
  walk(shared, (node) => {
    if (node.type !== "Property" || node.key.name !== "exclude") return;
    if (defaults) throw new Error("Ambiguous shared coverage exclusions");
    if (node.value.type !== "ArrayExpression") {
      throw new Error("Shared coverage exclusions must be a literal array");
    }
    const elements = node.value.elements.filter((element) => {
      if (element?.type !== "SpreadElement") return true;
      if (
        element.argument.type !== "Identifier" ||
        element.argument.name !== "exclude"
      ) {
        throw new Error("Unsupported shared coverage exclusion spread");
      }
      return false;
    });
    defaults = strings({ type: "ArrayExpression", elements }, sharedPath);
  });
  if (!defaults) throw new Error("Cannot find shared coverage exclusions");

  const collectors = [];
  for (const [path, text] of files) {
    if (!path.endsWith("/vite.config.ts")) continue;
    const parsed = sourceFile(path, text);
    const names = new Set(["bazelCoverage"]);
    let imported = false;
    for (const node of parsed.body) {
      if (node.type !== "ImportDeclaration") continue;
      for (const specifier of node.specifiers) {
        if (
          specifier.type === "ImportSpecifier" &&
          specifier.imported.name === "bazelCoverage"
        ) {
          names.add(specifier.local.name);
          imported = true;
        }
      }
    }
    const before = collectors.length;
    walk(parsed, (node) => {
      if (
        node.type !== "CallExpression" ||
        node.callee.type !== "Identifier" ||
        !names.has(node.callee.name)
      )
        return;
      if (node.arguments.length > 2)
        throw new Error(`Unsupported coverage arguments: ${path}`);
      collectors.push({
        source: path,
        base: path === "tools/bazel/vitest/vite.config.ts" ? "" : dirname(path),
        include: strings(node.arguments[0], path),
        exclude: [
          ...defaults.map((pattern) => ({ pattern, source: sharedPath })),
          ...(node.arguments[1] ? strings(node.arguments[1], path) : []).map(
            (pattern) => ({ pattern, source: path }),
          ),
        ],
      });
    });
    if (imported && before === collectors.length) {
      throw new Error(`Cannot find imported coverage collector call: ${path}`);
    }
  }
  if (!collectors.length)
    throw new Error("Cannot find bazelCoverage collectors");
  return collectors.sort((a, b) => a.source.localeCompare(b.source));
}

// Only syntax known to be erased is called non-executable. Unknown runtime
// declarations remain candidates; the collector determines executable lines.
function hasRuntime(statement) {
  if (
    statement.declare ||
    [
      "EmptyStatement",
      "TSInterfaceDeclaration",
      "TSTypeAliasDeclaration",
      "TSDeclareFunction",
    ].includes(statement.type)
  )
    return false;
  if (statement.type === "ImportDeclaration")
    return statement.importKind !== "type";
  if (statement.type === "ExportNamedDeclaration") {
    if (statement.exportKind === "type") return false;
    if (statement.declaration) return hasRuntime(statement.declaration);
    return Boolean(
      statement.source ||
      statement.specifiers.some((specifier) => specifier.exportKind !== "type"),
    );
  }
  if (statement.type === "ExportDefaultDeclaration")
    return hasRuntime(statement.declaration);
  return true;
}

function exclusion(path, inventory, collectors, uploadExclusions) {
  const scoped = inventory.exclude.find(({ pattern }) =>
    matchesGlob(path, pattern),
  );
  if (scoped) return { ...scoped, kind: "inventory" };
  const upload = uploadExclusions.find(({ pattern }) =>
    matchesGlob(path, pattern),
  );
  if (upload) return upload;
  if (path.endsWith(".java")) return undefined;
  // Collectors select emitted JS. Preserve original TS/TSX names in the report.
  const emitted = path.replace(/\.tsx?$/, ".js");
  const applicable = collectors.flatMap((collector) => {
    const scopes = inventory.collectorScopes?.[collector.source];
    if (scopes && !scopes.some((pattern) => matchesGlob(path, pattern)))
      return [];
    const local = collector.base
      ? posix.relative(collector.base, emitted)
      : emitted;
    if (!collector.include.some((pattern) => matchesGlob(local, pattern)))
      return [];
    const match = collector.exclude.find(({ pattern }) =>
      matchesGlob(pattern.endsWith(".d.ts") ? path : local, pattern),
    );
    return [{ collector, match }];
  });
  // An inclusion through any collector keeps this source in the denominator.
  if (!applicable.length || applicable.some(({ match }) => !match))
    return undefined;
  return {
    kind: "collector",
    reason: "Excluded by every applicable frontend collector.",
    rules: applicable.map(({ collector, match }) => ({
      collector: collector.source,
      ...match,
    })),
  };
}

export function executableEvidence(path, text) {
  if (path.endsWith(".java")) {
    // Java Unicode escapes are processed before lexing; text blocks also need
    // a full lexer. Keep these uncertain instead of trusting stripped tokens.
    if (/\\u|"""/.test(text)) {
      return {
        kind: "unknown",
        reason: "Java source requires compiler analysis.",
      };
    }
    const tokens = text.replace(
      /"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\/\*[\s\S]*?\*\/|\/\/[^\r\n]*/g,
      " ",
    );
    if (/^\s*(?:package\s+[\w.]+\s*;\s*)?$/.test(tokens)) {
      return { kind: "none", reason: "Empty or package-only Java source." };
    }
    if (
      /\b(return|throw|new|assert|if|for|while|switch|try)\b|->|\+\+|--/.test(
        tokens,
      )
    ) {
      return {
        kind: "runtime",
        reason:
          "Java source contains runtime operations; line count requires bytecode.",
      };
    }
    return {
      kind: "unknown",
      reason: "Java declarations may generate executable bytecode.",
    };
  }
  try {
    const parsed = sourceFile(path, text);
    if (path.endsWith(".d.ts"))
      return { kind: "none", reason: "TypeScript declaration file." };
    return parsed.body.some(hasRuntime)
      ? {
          kind: "runtime",
          reason:
            "TypeScript contains runtime syntax; LCOV line count is unknown.",
        }
      : {
          kind: "none",
          reason: "TypeScript contains only erased types or empty statements.",
        };
  } catch {
    return {
      kind: "unknown",
      reason: "TypeScript source could not be classified.",
    };
  }
}

export function createReport({
  files,
  lcov,
  root,
  inventory,
  collectors,
  uploadExclusions = [],
  run,
  revision,
}) {
  if (!run.trim()) throw new Error("A nonempty run label is required");
  const records = parseLcov(lcov, root);
  const sources = [...files.keys()]
    .filter((path) =>
      inventory.include.some((pattern) => matchesGlob(path, pattern)),
    )
    .sort();
  const rows = sources.map((path) => {
    const text = files.get(path);
    const lines = records.get(path);
    if (
      lines &&
      [...lines.keys()].some((number) => number > text.split(/\r?\n/).length)
    ) {
      throw new Error(`LCOV line exceeds source length: ${path}`);
    }
    const excluded = exclusion(path, inventory, collectors, uploadExclusions);
    const evidence = executableEvidence(path, text);
    const measuredLines = lines?.size ?? 0;
    const coveredLines = lines ? [...lines.values()].filter(Boolean).length : 0;
    const status = excluded
      ? "excluded"
      : measuredLines
        ? "measured"
        : evidence.kind === "runtime"
          ? "omitted-executable"
          : evidence.kind === "none"
            ? "non-executable"
            : "unresolved";
    return {
      path,
      status,
      evidence,
      exclusion: excluded,
      lcov: lines ? (measuredLines ? "lines" : "empty-record") : "absent",
      measuredLines,
      coveredLines,
    };
  });
  const summary = Object.fromEntries(
    statuses.map((status) => [
      status,
      rows.filter((row) => row.status === status).length,
    ]),
  );
  const measured = rows.filter(({ status }) => status === "measured");
  summary.measuredLines = measured.reduce(
    (sum, row) => sum + row.measuredLines,
    0,
  );
  summary.coveredLines = measured.reduce(
    (sum, row) => sum + row.coveredLines,
    0,
  );
  const sourceSet = new Set(sources);
  const classifier = `prettier-typescript-${parserVersion};runtime-syntax-v1;java-runtime-tokens-v1`;
  const unexpected = [...records]
    .filter(([path]) => !sourceSet.has(path))
    .map(([path, lines]) => ({ path, measuredLines: lines.size }))
    .sort((a, b) => a.path.localeCompare(b.path));
  return {
    schemaVersion,
    context: {
      run,
      revision,
      classifier,
      scopeFingerprint: digest({
        schemaVersion,
        inventory,
        collectors,
        uploadExclusions,
        classifier,
      }),
      inventoryFingerprint: digest(
        sources.map((path) => [path, digest(files.get(path))]),
      ),
      lcovFingerprint: digest(lcov),
    },
    summary,
    sources: rows,
    unexpected,
  };
}

export function compareReports(current, baseline) {
  if (
    baseline.schemaVersion !== schemaVersion ||
    baseline.context?.scopeFingerprint !== current.context.scopeFingerprint ||
    baseline.context?.classifier !== current.context.classifier ||
    baseline.context?.run !== current.context.run
  ) {
    throw new Error(
      "Baseline is not comparable: schema, inventory/exclusions/classifier, and run label must match",
    );
  }
  const previous = new Map(baseline.sources.map((row) => [row.path, row]));
  const present = new Map(current.sources.map((row) => [row.path, row]));
  return {
    baselineRevision: baseline.context.revision,
    inventoryChanged:
      baseline.context.inventoryFingerprint !==
      current.context.inventoryFingerprint,
    delta: Object.fromEntries(
      Object.keys(current.summary).map((key) => [
        key,
        current.summary[key] - baseline.summary[key],
      ]),
    ),
    added: [...present.keys()].filter((path) => !previous.has(path)),
    removed: [...previous.keys()].filter((path) => !present.has(path)),
    transitions: current.sources
      .filter(
        (row) =>
          previous.has(row.path) &&
          previous.get(row.path).status !== row.status,
      )
      .map((row) => ({
        path: row.path,
        from: previous.get(row.path).status,
        to: row.status,
      })),
  };
}

export function formatReport(report) {
  const lines = [
    `Coverage inventory — ${report.context.run}`,
    `Revision: ${report.context.revision}`,
    ...statuses.map((status) => `${status}: ${report.summary[status]} files`),
    `Measured executable lines: ${report.summary.coveredLines}/${report.summary.measuredLines} hit`,
    `Unexpected LCOV sources: ${report.unexpected.length}`,
    "Empty LCOV records do not establish measurement. Omitted line counts are unknown.",
    "Java runtime evidence is conservative; TS syntax is not the collector's source mapping.",
  ];
  if (report.comparison) {
    lines.push(
      `Comparison with ${report.comparison.baselineRevision}: ${JSON.stringify(report.comparison)}`,
    );
  }
  for (const row of report.sources) {
    lines.push(
      `${row.status}\t${row.path}\t${row.lcov}\t${row.coveredLines}/${row.measuredLines}\t${row.exclusion?.reason ?? row.evidence.reason}`,
    );
  }
  for (const row of report.unexpected) {
    lines.push(
      `outside-inventory\t${row.path}\t${row.measuredLines} measured lines`,
    );
  }
  return lines.join("\n") + "\n";
}
