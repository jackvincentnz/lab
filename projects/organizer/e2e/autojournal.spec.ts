import { expect, test } from "./fixtures";

test("automatically journals a completed task", async ({ page }) => {
  const task = Date.now().toString();

  await page.goto("/task");
  await page.locator('[data-test="add-task-input"]').fill(task);
  await page.locator('[data-test="add-task-button"]').click();
  await page.getByText(task, { exact: true }).click();
  await page.getByText("Journal", { exact: true }).click();

  await expect(page).toHaveURL(/\/journal/);
  await expect(
    page.getByText(`Completed: ${task}`, { exact: true }).first(),
  ).toBeVisible({ timeout: 30_000 });
});
