import { expect, test } from "./fixtures";

test("serves the web UI from the service without Node access", async ({
  window,
}) => {
  const nodeType = await window.evaluate(() => typeof process);

  await expect(window.getByRole("heading", { name: "Slate" })).toBeVisible();
  expect(nodeType).toBe("undefined");
});

test("shows the logo in the header", async ({ window }) => {
  const logo = window.locator("header img");

  await expect
    .poll(() => logo.evaluate((image: HTMLImageElement) => image.naturalWidth))
    .toBeGreaterThan(0);
});

test("lists an item added in the web UI", async ({ window }) => {
  const items = window
    .getByRole("list", { name: "Next" })
    .getByRole("listitem");

  const name = window.getByRole("textbox", { name: "New item" });
  await name.fill("Plan the week");
  await name.press("Enter");

  await expect(items).toHaveCount(1);
  await expect(items).toContainText("Plan the week");
});

test("stops every process it started on quit", async ({ slate }) => {
  const main = slate.process();
  const processes = await slate.evaluate(({ app }) => app.getAppMetrics());

  await slate.close();

  expect(processes).toContainEqual(
    expect.objectContaining({ type: "Utility", name: "Slate service" }),
  );
  expect(main.exitCode).toBe(0);
  // On Linux, Chromium's GPU helper can outlive the main process for a moment, and Node cannot wait
  // for processes it did not start.
  await expect
    .poll(() => processes.filter(({ pid }) => isRunning(pid)))
    .toEqual([]);
});

function isRunning(pid: number): boolean {
  try {
    process.kill(pid, 0);
    return true;
  } catch (error) {
    return (error as NodeJS.ErrnoException).code !== "ESRCH";
  }
}
