import { Rule, SchematicContext, Tree } from '@angular-devkit/schematics';

export const DEFAULT_NAME = 'hello';

export function helloWorld(options: any): Rule {
  return (tree: Tree, _context: SchematicContext) => {
    tree.create(options?.name || DEFAULT_NAME, 'world');
    return tree;
  };
}
