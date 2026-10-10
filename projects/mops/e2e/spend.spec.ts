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

test("loads saved chat through the app WebSocket proxy", async ({ page }) => {
  const updates: string[] = [];
  page.on("websocket", (socket) => {
    if (socket.url().endsWith("/ws/graphql")) {
      socket.on("framereceived", ({ payload }) =>
        updates.push(payload.toString()),
      );
    }
  });
  await page.goto("/spend");
  await page.getByRole("button", { name: "AI Chat", exact: true }).click();
  await page.getByRole("button", { name: "Previous Chats" }).click();
  await page
    .getByText("Create 2 budgets FY27 and FY28", { exact: true })
    .click();

  await expect(
    page.getByText(
      "I've created the FY27 budget, but the FY28 creation was rejected. What should I do next?",
      { exact: true },
    ),
  ).toBeVisible();
  expect(updates.some((frame) => frame.includes('"chatUpdated"'))).toBe(true);
});
