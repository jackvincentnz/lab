import { afterEach, expect, test, vi } from "@lab/test-utils";
import { createDayApi } from "./api";
import { item } from "./test/fakes";

const fetch = vi.fn<typeof globalThis.fetch>();

afterEach(() => {
  fetch.mockReset();
});

test("sends a new item's name to the service's addItem procedure", async () => {
  const added = item({ name: "Plan the week" });
  vi.stubGlobal("fetch", fetch);
  fetch.mockResolvedValue(Response.json({ result: { data: added } }));

  const result = await createDayApi("http://day.test/api").addItem(
    "Plan the week",
  );

  expect(result).toEqual(added);
  const [url, init] = fetch.mock.calls[0]!;
  expect(String(url)).toBe("http://day.test/api/addItem");
  expect(init?.body).toBe(JSON.stringify({ name: "Plan the week" }));
});

test("lists items through the service's listItems procedure", async () => {
  const items = [item(), item()];
  vi.stubGlobal("fetch", fetch);
  fetch.mockResolvedValue(Response.json({ result: { data: items } }));

  const result = await createDayApi("http://day.test/api").listItems();

  expect(result).toEqual(items);
  expect(String(fetch.mock.calls[0]![0])).toBe("http://day.test/api/listItems");
});

test("sends a change to the service's updateItem procedure", async () => {
  const updated = item({ status: "done" });
  vi.stubGlobal("fetch", fetch);
  fetch.mockResolvedValue(Response.json({ result: { data: updated } }));

  const result = await createDayApi("http://day.test/api").updateItem({
    id: updated.id,
    status: "done",
  });

  expect(result).toEqual(updated);
  const [url, init] = fetch.mock.calls[0]!;
  expect(String(url)).toBe("http://day.test/api/updateItem");
  expect(init?.body).toBe(JSON.stringify({ id: updated.id, status: "done" }));
});
