import { Tree } from "@angular-devkit/schematics";
import { SchematicTestRunner } from "@angular-devkit/schematics/testing";

import { DEFAULT_NAME, HelloWorldOptions } from "./index";

describe("hello-world", () => {
  const runner = new SchematicTestRunner(
    "schematics",
    require.resolve("../collection.json")
  );

  it("should create file with default name when not set", async () => {
    const { tree } = await setup();

    expect(tree.files).toEqual(["/" + DEFAULT_NAME]);
  });

  it("should create file with name when provided", async () => {
    const name = "some-name";
    const { tree } = await setup({ name });

    expect(tree.files).toEqual(["/" + name]);
  });

  async function setup(config?: HelloWorldOptions) {
    const tree = await runner
      .runSchematicAsync("hello-world", config, Tree.empty())
      .toPromise();

    return { tree };
  }
});
