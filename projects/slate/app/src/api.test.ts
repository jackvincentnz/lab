import { afterEach, describe, expect, it, test, vi } from "@lab/test-utils";
import { createTRPCOptionsProxy } from "@trpc/tanstack-react-query";
import type { SlateRouter } from "../../service/src/router.js";
import { affects, createQueryClient, createSlateClient } from "./api";
import { item } from "./test/fakes";

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

describe("affects", () => {
  const trpc = createTRPCOptionsProxy<SlateRouter>({
    client: createSlateClient("http://slate.test/api"),
    queryClient: createQueryClient(),
  });
  const listKey = trpc.listItems.queryKey();

  function itemKey(id: string) {
    return [["getItem"], { input: { id }, type: "query" }];
  }

  it("reloads the list after an item is added", () => {
    expect(affects({ name: "Plan the week" }, listKey)).toBe(true);
  });

  it("reloads the list after an item changes", () => {
    expect(affects({ id: crypto.randomUUID() }, listKey)).toBe(true);
  });

  it("reloads the changed item", () => {
    const id = crypto.randomUUID();

    expect(affects({ id, name: "Plan the month" }, itemKey(id))).toBe(true);
  });

  it("leaves other items alone", () => {
    const changed = { id: crypto.randomUUID(), name: "Plan the month" };

    expect(affects(changed, itemKey(crypto.randomUUID()))).toBe(false);
  });

  it("leaves items alone when one is added", () => {
    expect(
      affects({ name: "Plan the week" }, itemKey(crypto.randomUUID())),
    ).toBe(false);
  });
});
