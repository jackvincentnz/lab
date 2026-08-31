import { expect, test as base } from "@playwright/test";

export const test = base.extend({
  baseURL: async ({ browserName }, use) => {
    const baseURL = process.env["MOPS_E2E_BASE_URL"];
    if (!baseURL) {
      throw new Error(
        `MOPS_E2E_BASE_URL was not set before launching ${browserName}`,
      );
    }

    await use(baseURL);
  },
});

export { expect };
