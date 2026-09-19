import { expect, test } from "./fixtures";

test("redirects the root path to the tasks page", async ({ page }) => {
  await page.goto("/");

  await expect(page).toHaveURL(/\/task/);
});

test("loads the tasks page", async ({ page }) => {
  await page.goto("/task");

  await expect(page).toHaveURL(/\/task/);
});

test("loads the journal page", async ({ page }) => {
  await page.goto("/journal");

  await expect(page).toHaveURL(/\/journal/);
});
