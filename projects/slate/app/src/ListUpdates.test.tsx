import { focusManager, onlineManager } from "@tanstack/react-query";
import {
  act,
  afterEach,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  within,
} from "@lab/test-utils";
import type { Item } from "./api";
import { ListUpdates } from "./ListUpdates";
import { ItemsPage } from "./pages/ItemsPage";
import {
  type FakeSlateApi,
  fakeSlateApi,
  item,
  withSlateApi,
} from "./test/fakes";
import { layOutRowsVertically } from "./test/layout";

async function showList(api: FakeSlateApi) {
  render(
    <ListUpdates>
      <ItemsPage />
    </ListUpdates>,
    { wrapper: withSlateApi(api) },
  );
  await screen.findByRole("button", { name: "Open Plan the week" });
}

function changeElsewhere(api: FakeSlateApi) {
  api.listItems.mockResolvedValue([item({ name: "Book travel" })]);
  act(() => api.listChanged.change());
}

async function openItem(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByRole("button", { name: "Open Plan the week" }));
  return screen.getByRole("complementary", { name: "Item details" });
}

async function startDrag(user: ReturnType<typeof userEvent.setup>) {
  layOutRowsVertically();
  screen.getByRole("button", { name: "Move Plan the week" }).focus();
  await user.keyboard("[Space]");
}

async function startRename(user: ReturnType<typeof userEvent.setup>) {
  const panel = await openItem(user);
  await user.click(
    within(panel).getByRole("button", { name: "Rename Plan the week" }),
  );
  await user.type(within(panel).getByRole("textbox"), " today");
  return panel;
}

async function startDescription(user: ReturnType<typeof userEvent.setup>) {
  const panel = await openItem(user);
  await user.click(
    within(panel).getByRole("button", { name: "Add a description" }),
  );
  await user.type(within(panel).getByRole("textbox"), "For Monday");
}

async function startSource(user: ReturnType<typeof userEvent.setup>) {
  const panel = await openItem(user);
  await user.click(within(panel).getByRole("button", { name: "Add a source" }));
  await user.type(
    within(panel).getByRole("textbox", { name: "Link" }),
    "https://example.com",
  );
}

function twoItems() {
  return fakeSlateApi([
    item({ name: "Plan the week" }),
    item({ name: "Write the report" }),
  ]);
}

describe("ListUpdates", () => {
  afterEach(() => {
    focusManager.setFocused(undefined);
    onlineManager.setOnline(true);
  });

  it("shows a change made elsewhere as soon as the service announces it", async () => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);

    changeElsewhere(api);

    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("reloads the list on connecting, to show changes made while disconnected", async () => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    api.listItems.mockResolvedValue([item({ name: "Book travel" })]);

    act(() => api.listChanged.connect());

    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("holds a change while an item is dragged", async () => {
    const user = userEvent.setup();
    const api = twoItems();
    await showList(api);
    await startDrag(user);

    changeElsewhere(api);

    expect(api.listItems).toHaveBeenCalledOnce();
  });

  it("holds a change while an item is dragged into another group", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Write the report", status: "in_progress" }),
      item({ name: "Plan the week" }),
    ]);
    await showList(api);
    await startDrag(user);
    await user.keyboard("[ArrowUp]");

    changeElsewhere(api);

    expect(api.listItems).toHaveBeenCalledOnce();
    expect(
      within(screen.getByRole("list", { name: "In progress" })).getByRole(
        "button",
        {
          name: "Open Plan the week",
        },
      ),
    ).toBeInTheDocument();
  });

  it("shows a held change when the drag ends", async () => {
    const user = userEvent.setup();
    const api = twoItems();
    await showList(api);
    await startDrag(user);
    changeElsewhere(api);

    await user.keyboard("[Escape]");

    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("shows a held change when the dragged item is dropped", async () => {
    const user = userEvent.setup();
    const api = twoItems();
    await showList(api);
    await startDrag(user);
    changeElsewhere(api);

    await user.keyboard("[Space]");

    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("keeps a rename's text when its item is deleted elsewhere", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    await startRename(user);
    api.listItems.mockResolvedValue([]);

    act(() => api.listChanged.change());

    expect(api.listItems).toHaveBeenCalledOnce();
    expect(screen.getByRole("textbox", { name: "Name" })).toHaveValue(
      "Plan the week today",
    );
  });

  it("shows a change held during a rename when the rename ends", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    await startRename(user);
    api.listItems.mockResolvedValue([]);
    act(() => api.listChanged.change());

    await user.keyboard("{Escape}");

    expect(
      await screen.findByText(/Nothing on your slate yet/),
    ).toBeInTheDocument();
  });

  it("holds a change while a description is edited", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    await startDescription(user);

    changeElsewhere(api);

    expect(api.listItems).toHaveBeenCalledOnce();
  });

  it("shows a change held during a description edit when the edit ends", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    await startDescription(user);
    changeElsewhere(api);

    await user.keyboard("{Escape}");

    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("holds a change while a source is edited", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    await startSource(user);

    changeElsewhere(api);

    expect(api.listItems).toHaveBeenCalledOnce();
    expect(screen.getByRole("textbox", { name: "Link" })).toHaveValue(
      "https://example.com",
    );
  });

  it("shows a change held during a source edit when the edit ends", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    await startSource(user);
    changeElsewhere(api);

    await user.keyboard("{Escape}");

    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("keeps a rename's text when the page becomes visible after its item is deleted elsewhere", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    await startRename(user);
    api.listItems.mockResolvedValue([]);

    await act(async () => {
      focusManager.setFocused(false);
      focusManager.setFocused(true);
      await Promise.resolve();
    });

    expect(api.listItems).toHaveBeenCalledOnce();
    expect(screen.getByRole("textbox", { name: "Name" })).toHaveValue(
      "Plan the week today",
    );
  });

  it("reloads the list when the page becomes visible", async () => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    api.listItems.mockResolvedValue([item({ name: "Book travel" })]);

    act(() => {
      focusManager.setFocused(false);
      focusManager.setFocused(true);
    });

    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("keeps a rename's text when a reload already under way removes its item", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    let finishReload: (items: Item[]) => void = () => undefined;
    const reloaded = new Promise<Item[]>((resolve) => (finishReload = resolve));
    api.listItems.mockReturnValue(reloaded);
    act(() => api.listChanged.change());
    await startRename(user);

    await act(async () => {
      finishReload([]);
      // The reload's result reaches the page a few tasks after the response.
      await new Promise((settled) => setTimeout(settled, 50));
    });

    expect(screen.getByRole("textbox", { name: "Name" })).toHaveValue(
      "Plan the week today",
    );
  });

  it("keeps a rename's text when the browser comes back online after its item is deleted elsewhere", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    await startRename(user);
    api.listItems.mockResolvedValue([]);

    await act(async () => {
      onlineManager.setOnline(false);
      onlineManager.setOnline(true);
      await Promise.resolve();
    });

    expect(api.listItems).toHaveBeenCalledOnce();
  });
});
