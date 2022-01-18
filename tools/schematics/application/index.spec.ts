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
    version: '13.1.2',
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
    const workspaceFiles = [...workspaceTree.files];
    const tree = await runner
      .runSchematicAsync('application', defaultOptions, workspaceTree)
      .toPromise();

    expect(tree.files.length).not.toEqual(workspaceFiles.length);
  });

  it('should create all files under new project root', async () => {
    const workspaceFiles = [...workspaceTree.files];
    const tree = await runner
      .runSchematicAsync('application', defaultOptions, workspaceTree)
      .toPromise();

    const nonWorkspaceFiles = tree.files.filter(
      (file) => !workspaceFiles.includes(file)
    );

    nonWorkspaceFiles.forEach((file) =>
      expect(file.startsWith('/apps/foo')).toBe(true, file)
    );
  });
});
