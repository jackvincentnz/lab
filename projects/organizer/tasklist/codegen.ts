import { clientCodegenConfig } from "../../../tools/bazel/graphql/codegen.ts";

export default clientCodegenConfig({
  schema: "../task/src/main/resources/schema/schema.graphqls",
});
