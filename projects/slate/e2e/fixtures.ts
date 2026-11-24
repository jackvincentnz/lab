import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import {
  type ElectronApplication,
  expect,
  type Page,
  test as base,
} from "@playwright/test";

const appDir = "projects/slate/app_dir";

const serviceUrl = /^http:\/\/127\.0\.0\.1:\d+\/$/;

interface SlateFixtures {
  slate: ElectronApplication;
  window: Page;
}

export const test = base.extend<SlateFixtures>({
  slate: async ({ playwright }, use) => {
    const slate = await playwright._electron.launch({
      args: [appDir],
      env: {
        ...process.env,
        SLATE_DATA_DIR: await mkdtemp(join(tmpdir(), "slate-e2e-")),
        SLATE_PORT: "0",
      },
    });
    // firstWindow() returns before the window has loaded the web UI.
    await (await slate.firstWindow()).waitForURL(serviceUrl);

    await use(slate);

    await slate.close();
  },

  window: async ({ slate }, use) => {
    await use(await slate.firstWindow());
  },
});

export { expect };
