import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import {
  type ElectronApplication,
  expect,
  type Page,
  test as base,
} from "@playwright/test";

const appDir = "projects/day/app_dir";

const serviceUrl = /^http:\/\/127\.0\.0\.1:\d+\/$/;

interface DayFixtures {
  day: ElectronApplication;
  window: Page;
}

export const test = base.extend<DayFixtures>({
  day: async ({ playwright }, use) => {
    const day = await playwright._electron.launch({
      args: [appDir],
      env: {
        ...process.env,
        DAY_DATA_DIR: await mkdtemp(join(tmpdir(), "day-e2e-")),
        DAY_PORT: "0",
      },
    });
    // firstWindow() returns before the window has loaded the web UI.
    await (await day.firstWindow()).waitForURL(serviceUrl);

    await use(day);

    await day.close();
  },

  window: async ({ day }, use) => {
    await use(await day.firstWindow());
  },
});

export { expect };
