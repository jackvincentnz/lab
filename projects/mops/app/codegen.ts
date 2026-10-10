import { clientCodegenConfig } from "../../../tools/bazel/graphql/codegen.ts";

export default clientCodegenConfig({
  schema: "../service/src/main/resources/schema/schema.graphqls",
  scalars: { Date: "string", BigDecimal: "number" },
});
