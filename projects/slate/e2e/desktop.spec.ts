import type { Page } from "@playwright/test";
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

test("draws an item dragged out of In progress over the items in Next", async ({
  window,
}) => {
  const dragged = window
    .getByRole("listitem")
    .filter({ hasText: "Write the report" });
  await addItemsInWebUi(window, [
    "Book travel",
    "Plan the week",
    "Write the report",
  ]);
  await window
    .getByRole("button", { name: "Move Write the report" })
    .dragTo(window.getByText("Drag an item here when you start it"));
  await expect(window.getByRole("list", { name: "In progress" })).toContainText(
    "Write the report",
  );

  await holdDragOver(window, "Write the report", "Book travel");
  await expect(window.getByRole("list", { name: "Next" })).toContainText(
    "Write the report",
  );
  const drawn = await dragged.evaluate(
    (row, other) => {
      const { left, right, top } = row.getBoundingClientRect();
      const { bottom } = other!.getBoundingClientRect();
      return row.contains(
        document.elementFromPoint((left + right) / 2, (top + bottom) / 2),
      );
    },
    await window
      .getByRole("listitem")
      .filter({ hasText: "Book travel" })
      .elementHandle(),
  );

  expect(drawn).toBe(true);
});

async function addItemsInWebUi(window: Page, names: string[]): Promise<void> {
  const name = window.getByRole("textbox", { name: "New item" });
  for (const item of names) {
    await name.fill(item);
    await name.press("Enter");
    await expect(window.getByText(item)).toBeVisible();
  }
}

async function holdDragOver(
  window: Page,
  name: string,
  target: string,
): Promise<void> {
  const handle = await window
    .getByRole("button", { name: `Move ${name}` })
    .boundingBox();
  const over = await window
    .getByRole("listitem")
    .filter({ hasText: target })
    .boundingBox();
  const x = handle!.x + handle!.width / 2;

  await window.mouse.move(x, handle!.y + handle!.height / 2);
  await window.mouse.down();
  await window.mouse.move(x, over!.y + over!.height / 2, { steps: 10 });
}

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

test("opens a claude reference outside the app when its label is clicked", async ({
  slate,
  window,
}) => {
  await slate.evaluate(({ shell }) => {
    const opened: string[] = [];
    Object.assign(globalThis, { opened });
    shell.openExternal = async (url) => {
      opened.push(url);
    };
  });
  await addItem(window.url(), {
    name: "Pick up the session",
    source: {
      kind: "reference",
      text: "claude://example/session",
      label: "Claude Desktop session",
    },
  });

  await window.getByText("Pick up the session").click();
  await window.getByRole("link", { name: "Claude Desktop session" }).click();

  await expect
    .poll(() =>
      slate.evaluate(() => (globalThis as { opened?: string[] }).opened),
    )
    .toEqual(["claude://example/session"]);
  expect(slate.windows()).toHaveLength(1);
});

async function addItem(serviceUrl: string, item: object): Promise<void> {
  const response = await fetch(new URL("mcp", serviceUrl), {
    method: "POST",
    headers: {
      "content-type": "application/json",
      accept: "application/json, text/event-stream",
    },
    body: JSON.stringify({
      jsonrpc: "2.0",
      id: 1,
      method: "tools/call",
      params: { name: "add_item", arguments: item },
    }),
  });
  const { result } = (await response.json()) as {
    result: { isError?: boolean };
  };
  expect(result.isError).toBeUndefined();
}
