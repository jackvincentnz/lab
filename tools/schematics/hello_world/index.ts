import { Rule, Tree } from "@angular-devkit/schematics";

export const DEFAULT_NAME = "hello";

export interface HelloWorldOptions {
  readonly name?: string;
}

export function helloWorld(options: HelloWorldOptions): Rule {
  return (tree: Tree) => {
    tree.create(options?.name || DEFAULT_NAME, "world");
    return tree;
  };
}
