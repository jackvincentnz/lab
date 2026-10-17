import type { CodegenConfig } from "@graphql-codegen/cli";

interface ClientCodegenOptions {
  /** Schema path relative to the app root. */
  schema: string;
  /** TypeScript types for the schema's custom scalars. */
  scalars?: Record<string, string>;
}

/** Client-preset config for an app with documents in src and output in src/__generated__. */
export function clientCodegenConfig({
  schema,
  scalars,
}: ClientCodegenOptions): CodegenConfig {
  return {
    schema,
    documents: ["./src/**/*.gql"],
    generates: {
      "./src/__generated__/": {
        preset: "client",
        plugins: [],
        presetConfig: {
          gqlTagName: "gql",
        },
        config: {
          useTypeImports: true,
          ...(scalars && { scalars }),
        },
      },
    },
    ignoreNoDocuments: true,
  };
}
