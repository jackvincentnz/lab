import { CodegenConfig } from "@graphql-codegen/cli";

const config: CodegenConfig = {
  schema: "../../services/mops/src/main/resources/schema/schema.graphqls",
  documents: ["./src/**/*.gql"],
  generates: {
    "./src/__generated__/": {
      preset: "client",
      plugins: [],
      presetConfig: {
        gqlTagName: "gql",
      },
      config: {
        scalars: {
          Date: "string",
          BigDecimal: "number",
        },
      },
    },
  },
  ignoreNoDocuments: true,
};

export default config;
