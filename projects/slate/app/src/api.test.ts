import { afterEach, expect, test, vi } from "@lab/test-utils";
import { createSlateClient } from "./api";
import { item } from "./test/items";

const fetch = vi.fn<typeof globalThis.fetch>();

afterEach(() => {
  fetch.mockReset();
});

test("calls the service's procedures under the given URL", async () => {
  const added = item({ name: "Plan the week" });
  vi.stubGlobal("fetch", fetch);
  fetch.mockResolvedValue(Response.json({ result: { data: added } }));

  const result = await createSlateClient(
    "http://slate.test/api",
  ).addItem.mutate({
    name: "Plan the week",
  });

  expect(result).toEqual(added);
  const [url, init] = fetch.mock.calls[0]!;
  expect(String(url)).toBe("http://slate.test/api/addItem");
  expect(init?.body).toBe(JSON.stringify({ name: "Plan the week" }));
});
