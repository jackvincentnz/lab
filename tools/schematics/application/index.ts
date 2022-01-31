import {
  Rule,
  SchematicContext,
  Tree,
  chain,
  externalSchematic,
} from "@angular-devkit/schematics";

// Follows example at https://blog.angular.io/schematics-an-introduction-dc1dfbc2a2b2
export function application(options: any): Rule {
  return chain([
    externalSchematic("@schematics/angular", "application", options),
    (tree: Tree, _context: SchematicContext) => {
      return tree;
    },
  ]);
}
