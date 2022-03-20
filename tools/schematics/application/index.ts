import {
  Rule,
  Tree,
  chain,
  externalSchematic,
} from "@angular-devkit/schematics";
import { Schema as ApplicationOptions } from "@schematics/angular/application/schema";

// Follows example at https://blog.angular.io/schematics-an-introduction-dc1dfbc2a2b2
export function application(options: ApplicationOptions): Rule {
  return chain([
    externalSchematic("@schematics/angular", "application", options),
    (tree: Tree) => {
      return tree;
    },
  ]);
}
