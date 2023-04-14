/**
 * Based on configuration from @anglar-eslint.
 * See https://github.com/angular-eslint/angular-eslint#notes-on-eslint-configuration-itself for information on performant configurations.
 */
module.exports = {
  root: true,
  env: {
    browser: true,
    node: true,
  },
  overrides: [
    {
      files: ["*.tsx", "*.ts"],
      // Use recommended linting as much as possible to minimize opinionated customization.
      extends: [
        "eslint:recommended",

        "plugin:@typescript-eslint/recommended",

        "plugin:import/recommended",
        "plugin:import/typescript",

        "plugin:@angular-eslint/recommended",
        "plugin:@angular-eslint/template/process-inline-templates",

        "prettier",
      ],
      rules: {
        "@angular-eslint/directive-selector": [
          "error",
          {
            type: "attribute",
            prefix: "app",
            style: "camelCase",
          },
        ],
        "@angular-eslint/component-selector": [
          "error",
          {
            type: "element",
            prefix: "app",
            style: "kebab-case",
          },
        ],
      },
    },
    {
      files: ["*.js", "*.jsx"],
      // Use recommended linting as much as possible to minimize opinionated customization.
      extends: ["eslint:recommended", "plugin:import/recommended", "prettier"],
    },
    {
      files: ["*.html"],
      extends: ["plugin:@angular-eslint/template/recommended"],
    },
  ],
};
