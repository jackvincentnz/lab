import { clientCodegenConfig } from "../../../tools/bazel/graphql/codegen.ts";

export default clientCodegenConfig({
  schema: "../journal/src/main/resources/schema/schema.graphqls",
});
