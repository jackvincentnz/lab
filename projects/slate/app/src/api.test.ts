import { afterEach, expect, test, vi } from "@lab/test-utils";
import { createTRPCOptionsProxy } from "@trpc/tanstack-react-query";
import type { SlateRouter } from "../../service/src/router.js";
import {
  changeAffectsQuery,
  createQueryClient,
  createSlateClient,
} from "./api";
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

class FakeEventSource extends EventTarget {
  static opened: FakeEventSource[] = [];
  readonly CLOSED = 2;
  readyState = 0;
  close = vi.fn();
  readonly url: string;

  constructor(url: string) {
    super();
    this.url = url;
    FakeEventSource.opened.push(this);
  }
}

function listenForChanges() {
  FakeEventSource.opened = [];
  vi.stubGlobal("EventSource", FakeEventSource);
  return createSlateClient("http://slate.test/api").listChanged.subscribe(
    undefined,
    { onData: () => undefined },
  );
}

test("listens for list changes under the given URL", async () => {
  const subscription = listenForChanges();

  await vi.waitFor(() =>
    expect(FakeEventSource.opened.map(({ url }) => url)).toEqual([
      "http://slate.test/api/listChanged",
    ]),
  );
  subscription.unsubscribe();
});

test("listens again after the connection fails for good", async () => {
  vi.useFakeTimers();
  const subscription = listenForChanges();
  await vi.waitFor(() => expect(FakeEventSource.opened).toHaveLength(1));
  const [failed] = FakeEventSource.opened;

  failed!.readyState = failed!.CLOSED;
  failed!.dispatchEvent(new Event("error"));
  await vi.advanceTimersByTimeAsync(3000);

  expect(FakeEventSource.opened).toHaveLength(2);
  subscription.unsubscribe();
});

const trpc = createTRPCOptionsProxy<SlateRouter>({
  client: createSlateClient("http://slate.test/api"),
  queryClient: createQueryClient(),
});
const listKey = trpc.listItems.queryKey();

function itemKey(id: string) {
  return [["getItem"], { input: { id }, type: "query" }];
}

test("adding an item reloads the list", () => {
  expect(changeAffectsQuery({ name: "Plan the week" }, listKey)).toBe(true);
});

test("changing an item reloads the list", () => {
  expect(changeAffectsQuery({ id: crypto.randomUUID() }, listKey)).toBe(true);
});

test("changing an item reloads that item", () => {
  const id = crypto.randomUUID();

  expect(changeAffectsQuery({ id, name: "Plan the month" }, itemKey(id))).toBe(
    true,
  );
});

test("changing an item leaves other items alone", () => {
  const changed = { id: crypto.randomUUID(), name: "Plan the month" };

  expect(changeAffectsQuery(changed, itemKey(crypto.randomUUID()))).toBe(false);
});

test("adding an item leaves other items alone", () => {
  expect(
    changeAffectsQuery({ name: "Plan the week" }, itemKey(crypto.randomUUID())),
  ).toBe(false);
});
