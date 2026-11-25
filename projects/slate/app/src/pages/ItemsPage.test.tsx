import {
  afterEach,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  waitFor,
  within,
} from "@lab/test-utils";
import { TestSlate, withSlate } from "../test/slate";
import { layOutRowsVertically } from "../test/layout";
import { ItemsPage } from "./ItemsPage";
import { onlineManager } from "@tanstack/react-query";

function itemNames() {
  return screen
    .getAllByRole("listitem")
    .map((row) => within(row).getByRole("button", { name: /^Move / }))
    .map((handle) => handle.getAttribute("aria-label")!.slice("Move ".length));
}

async function moveWithKeyboard(
  user: ReturnType<typeof userEvent.setup>,
  name: string,
  direction: "[ArrowUp]" | "[ArrowDown]",
) {
  layOutRowsVertically();
  (await screen.findByRole("button", { name: `Move ${name}` })).focus();
  await user.keyboard(`[Space]${direction}[Space]`);
}

describe("ItemsPage", () => {
  afterEach(() => {
    onlineManager.setOnline(true);
  });

  it("shows a loader until the list arrives", () => {
    const slate = new TestSlate();
    slate.hold("listItems");

    render(<ItemsPage />, { wrapper: withSlate(slate) });

    expect(screen.getByLabelText("Loading your list")).toBeInTheDocument();
  });

  it("shows the service's items", async () => {
    const slate = new TestSlate();
    slate.store.add("Plan the week");
    slate.store.add("Book travel");

    render(<ItemsPage />, { wrapper: withSlate(slate) });

    expect(await screen.findAllByRole("listitem")).toHaveLength(2);
    expect(itemNames()).toEqual(["Plan the week", "Book travel"]);
  });

  it("says the list is empty when there are no items", async () => {
    render(<ItemsPage />, { wrapper: withSlate(new TestSlate()) });

    expect(
      await screen.findByText(/Nothing on your slate yet/),
    ).toBeInTheDocument();
  });

  it("loads the list again on retry after it failed to load", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    slate.store.add("Plan the week");
    slate.fail("listItems", 1);
    render(<ItemsPage />, { wrapper: withSlate(slate) });

    await user.click(await screen.findByRole("button", { name: "Retry" }));

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(
      screen.queryByText("Could not load your list"),
    ).not.toBeInTheDocument();
  });

  it("adds an item and shows the service's new list", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    render(<ItemsPage />, { wrapper: withSlate(slate) });

    await user.type(
      await screen.findByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "New item" })).toHaveValue("");
    expect(slate.store.list()).toEqual([
      expect.objectContaining({ name: "Plan the week" }),
    ]);
  });

  it("renames an item and shows the service's new list", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    const original = slate.store.add("Plan the week");
    render(<ItemsPage />, { wrapper: withSlate(slate) });

    await user.click(
      await screen.findByRole("button", { name: "Rename Plan the week" }),
    );
    const name = screen.getByRole("textbox", { name: "Name" });
    await user.clear(name);
    await user.type(name, "Plan the month{Enter}");

    expect(await screen.findByText("Plan the month")).toBeInTheDocument();
    expect(
      screen.queryByRole("textbox", { name: "Name" }),
    ).not.toBeInTheDocument();
    expect(slate.store.list()).toEqual([
      expect.objectContaining({ id: original.id, name: "Plan the month" }),
    ]);
  });

  it("changes an item's status and shows the service's new list", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    const original = slate.store.add("Plan the week");
    render(<ItemsPage />, { wrapper: withSlate(slate) });

    await user.selectOptions(
      await screen.findByRole("combobox", { name: "Status of Plan the week" }),
      "Doing",
    );

    await waitFor(() =>
      expect(slate.store.list()).toEqual([
        expect.objectContaining({
          id: original.id,
          name: "Plan the week",
          status: "doing",
        }),
      ]),
    );
    expect(
      await screen.findByRole("combobox", { name: "Status of Plan the week" }),
    ).toHaveValue("doing");
  });

  it("shows a moved item in its new place while the move saves", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    ["Plan the week", "Book travel", "Write the report"].forEach((name) =>
      slate.store.add(name),
    );
    slate.hold("moveItem");
    render(<ItemsPage />, { wrapper: withSlate(slate) });

    await moveWithKeyboard(user, "Write the report", "[ArrowUp]");

    expect(itemNames()).toEqual([
      "Plan the week",
      "Write the report",
      "Book travel",
    ]);
  });

  it("keeps focus on a moved item for the next keyboard move", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    slate.store.add("Plan the week");
    slate.store.add("Book travel");
    slate.hold("moveItem");
    render(<ItemsPage />, { wrapper: withSlate(slate) });

    await moveWithKeyboard(user, "Plan the week", "[ArrowDown]");

    await waitFor(() =>
      expect(
        screen.getByRole("button", { name: "Move Plan the week" }),
      ).toHaveFocus(),
    );
  });

  it("moves an item and shows the service's new list", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    ["Plan the week", "Book travel", "Write the report"].forEach((name) =>
      slate.store.add(name),
    );
    render(<ItemsPage />, { wrapper: withSlate(slate) });
    await screen.findByText("Plan the week");
    // A change from elsewhere shows only if the page reloads the service's list.
    slate.store.add("Call the bank");

    await moveWithKeyboard(user, "Plan the week", "[ArrowDown]");

    await waitFor(() =>
      expect(itemNames()).toEqual([
        "Book travel",
        "Plan the week",
        "Write the report",
        "Call the bank",
      ]),
    );
  });

  it("puts an item back and shows an error when a move fails", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    slate.store.add("Plan the week");
    slate.store.add("Book travel");
    slate.fail("moveItem");
    render(<ItemsPage />, { wrapper: withSlate(slate) });

    await moveWithKeyboard(user, "Plan the week", "[ArrowDown]");

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(itemNames()).toEqual(["Plan the week", "Book travel"]);
  });

  it("keeps reaching the service while the browser is offline", async () => {
    const user = userEvent.setup();
    render(<ItemsPage />, { wrapper: withSlate(new TestSlate()) });
    const name = await screen.findByRole("textbox", { name: "New item" });
    onlineManager.setOnline(false);

    await user.type(name, "Plan the week{Enter}");

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
  });

  it("sends one change at a time", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    slate.hold("addItem");
    render(<ItemsPage />, { wrapper: withSlate(slate) });

    await user.type(
      await screen.findByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}{Enter}",
    );

    expect(slate.calls("addItem")).toBe(1);
    expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
  });

  it("shows an error and keeps what was typed when a change fails", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    slate.fail("addItem");
    render(<ItemsPage />, { wrapper: withSlate(slate) });

    await user.type(
      await screen.findByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "New item" })).toHaveValue(
      "Plan the week",
    );
  });

  it("clears a failed rename's error when the rename is cancelled", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    slate.store.add("Plan the week");
    slate.fail("updateItem");
    render(<ItemsPage />, { wrapper: withSlate(slate) });
    await user.click(
      await screen.findByRole("button", { name: "Rename Plan the week" }),
    );
    await user.type(
      screen.getByRole("textbox", { name: "Name" }),
      " soon{Enter}",
    );
    await screen.findByText("Could not save your change");

    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(
      screen.queryByText("Could not save your change"),
    ).not.toBeInTheDocument();
  });

  it("keeps offering a retry when a change fails before the list loads", async () => {
    const user = userEvent.setup();
    const slate = new TestSlate();
    slate.fail("listItems");
    slate.fail("addItem");
    render(<ItemsPage />, { wrapper: withSlate(slate) });
    await screen.findByRole("button", { name: "Retry" });

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Retry" })).toBeInTheDocument();
  });
});
