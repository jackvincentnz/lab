import {
  SchematicTestRunner,
  UnitTestTree,
} from '@angular-devkit/schematics/testing';
import { Schema as WorkspaceOptions } from '@schematics/angular/workspace/schema';
import { Schema as ApplicationOptions } from '@schematics/angular/application/schema';

describe('application', () => {
  const angularSchematics = new SchematicTestRunner(
    'schematics',
    require.resolve('@schematics/angular/collection.json')
  );
  const runner = new SchematicTestRunner(
    'schematics',
    require.resolve('../collection.json')
  );

  const workspaceOptions: WorkspaceOptions = {
    name: 'workspace',
    newProjectRoot: 'apps',
    version: '10.1.1',
  };

  const defaultOptions: ApplicationOptions = {
    name: 'foo',
  };

  let workspaceTree: UnitTestTree;
  beforeEach(async () => {
    workspaceTree = await angularSchematics
      .runSchematicAsync('workspace', workspaceOptions)
      .toPromise();
  });

  it('should create non workspace files', async () => {
    const tree = await runner
      .runSchematicAsync('application', defaultOptions, workspaceTree)
      .toPromise();

    expect(tree.files.length).not.toEqual(workspaceTree.files.length);
  });

  it('should create all files under new project root', async () => {
    const tree = await runner
      .runSchematicAsync('application', defaultOptions, workspaceTree)
      .toPromise();

    const nonWorkspaceFiles = tree.files.filter(
      (file) => !workspaceTree.files.includes(file)
    );

    nonWorkspaceFiles.forEach((file) =>
      expect(file.startsWith('/apps/foo')).toBe(true, file)
    );
  });
});
