// @ts-check

import { createRequire } from "node:module";

// resolves from config file path
const configRequire = createRequire(import.meta.url);

// resolves from runtime file path
const runtimeRequire = (() => {
  const argv1 = globalThis.process?.argv?.[1];
  if (!argv1) {
    return configRequire;
  }

  try {
    return createRequire(argv1);
  } catch {
    return configRequire;
  }
})();

function loadPackage(specifier) {
  try {
    // load first from node_modules nearest this file
    return configRequire(specifier);
  } catch {
    // fall back to node_modules installed near runtime path (pre-commit environment)
    return runtimeRequire(specifier);
  }
}

const eslint = loadPackage("@eslint/js");
const tseslint = loadPackage("typescript-eslint");
const globals = loadPackage("globals");
const eslintConfigPrettier = loadPackage("eslint-config-prettier/flat");
const eslintConfigApi = loadPackage("eslint/config");
const defineConfig =
  eslintConfigApi.defineConfig ?? eslintConfigApi.default?.defineConfig;

export default defineConfig(
  {
    ignores: ["**/node_modules/", "**/dist/"],
  },
  eslint.configs.recommended,
  tseslint.configs.recommended,
  tseslint.configs.stylistic,
  {
    files: ["**/*.{js,cjs}"],
    languageOptions: {
      globals: globals.node,
    },
    rules: {
      "@typescript-eslint/no-require-imports": "off",
    },
  },
  eslintConfigPrettier,
);
