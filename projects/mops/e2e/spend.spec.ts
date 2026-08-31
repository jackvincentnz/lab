import { expect, test } from "./fixtures";

test("renders spend data returned by the backend", async ({ page }) => {
  await page.goto("/spend");

  await expect(page).toHaveURL(/\/spend$/);
  await expect(
    page.getByText("Budget Category", { exact: true }),
  ).toBeVisible();
  await expect(
    page.getByText("Salaries, Benefits, Taxes (15 FTEs)", { exact: true }),
  ).toBeVisible();
});
