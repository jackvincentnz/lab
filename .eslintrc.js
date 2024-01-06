module.exports = {
  root: true,
  env: {
    browser: true,
    es2020: true,
    node: true,
  },
  parserOptions: {
    ecmaVersion: "latest",
    sourceType: "module",
  },
  extends: ["eslint:recommended", "prettier"],
  overrides: [
    {
      files: ["*.tsx", "*.ts"],
      // Use recommended linting as much as possible to minimize opinionated customization.
      extends: [
        "plugin:@typescript-eslint/stylistic",
        "plugin:@typescript-eslint/strict",
      ],
      parser: "@typescript-eslint/parser",
      plugins: ["@typescript-eslint"],
    },
    {
      files: ["*.tsx", "*.jsx"],
      settings: {
        react: {
          version: "detect",
        },
      },
      extends: ["plugin:react/recommended", "plugin:react/jsx-runtime"],
    },
  ],
};
