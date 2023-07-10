import { CodegenConfig } from "@graphql-codegen/cli";

const config: CodegenConfig = {
  schema: "../../contexts/task/src/main/resources/graphql/schema.gqls",
  documents: ["./src/**/*.gql"],
  generates: {
    "./src/__generated__/": {
      preset: "client",
      plugins: [],
      presetConfig: {
        gqlTagName: "gql",
      },
    },
  },
  ignoreNoDocuments: true,
};

export default config;
