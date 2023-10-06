import { CodegenConfig } from "@graphql-codegen/cli";

// TODO: generate this or extract to common config?
const config: CodegenConfig = {
  schema: "../../contexts/journal/src/main/resources/schema/schema.graphqls",
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
