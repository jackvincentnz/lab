import {
  act,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
  type Mock,
} from "@lab/test-utils";
import type { Item } from "../api";
import { item } from "../test/fakes";
import { ItemName } from "./ItemName";

type ItemNameProps = Parameters<typeof ItemName>[0];
type ChangeState = Pick<ItemNameProps, "disabled" | "saving">;

const idle: ChangeState = { disabled: false, saving: false };

function renderName(shownItem: Item = item({ name: "Plan the week" })) {
  const handlers = {
    onRename: vi.fn<ItemNameProps["onRename"]>(),
    onRenameCancel: vi.fn<ItemNameProps["onRenameCancel"]>(),
  };
  const name = (shown: Item, state: ChangeState) => (
    <ItemName item={shown} {...state} {...handlers} />
  );
  const { rerender } = render(name(shownItem, idle));
  return {
    ...handlers,
    disable: () => rerender(name(shownItem, { ...idle, disabled: true })),
    startSaving: () => rerender(name(shownItem, { ...idle, saving: true })),
    showItem: (shown: Item) => rerender(name(shown, idle)),
  };
}

function renameCallbacks(onRename: Mock<ItemNameProps["onRename"]>) {
  const [, , callbacks] = onRename.mock.lastCall!;
  return callbacks;
}

async function startRename(user: ReturnType<typeof userEvent.setup>) {
  await user.click(
    screen.getByRole("button", { name: "Rename Plan the week" }),
  );
  return screen.getByRole("textbox", { name: "Name" });
}

describe("ItemName", () => {
  it("shows the item's name as a heading", () => {
    renderName();

    expect(
      screen.getByRole("heading", { name: "Plan the week" }),
    ).toBeInTheDocument();
  });

  it("closes the rename once the new name is saved", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename } = renderName(original);
    onRename.mockImplementation((_editedFrom, name, { onSaved }) =>
      onSaved({ ...original, name }),
    );
    const name = await startRename(user);

    await user.type(name, " soon{Enter}");

    expect(onRename).toHaveBeenCalledWith(
      original,
      "Plan the week soon",
      expect.anything(),
    );
    expect(
      screen.queryByRole("textbox", { name: "Name" }),
    ).not.toBeInTheDocument();
  });

  it("keeps the rename open until it is saved", async () => {
    const user = userEvent.setup();
    renderName();
    const name = await startRename(user);

    await user.type(name, " soon{Enter}");

    expect(screen.getByRole("textbox", { name: "Name" })).toHaveValue(
      "Plan the week soon",
    );
  });

  it("keeps newer edits when an earlier rename is saved", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename } = renderName(original);
    const name = await startRename(user);
    await user.type(name, " soon{Enter}");
    await user.type(name, "er");
    const { onSaved } = renameCallbacks(onRename);

    act(() => onSaved({ ...original, name: "Plan the week soon" }));

    expect(screen.getByRole("textbox", { name: "Name" })).toHaveValue(
      "Plan the week sooner",
    );
  });

  it("saves newer edits from the item an earlier save returned", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename } = renderName(original);
    const name = await startRename(user);
    await user.type(name, " soon{Enter}");
    await user.type(name, "er");
    const saved = {
      ...original,
      name: "Plan the week soon",
      updatedAt: "2026-11-22T10:00:00.000Z",
    };
    act(() => renameCallbacks(onRename).onSaved(saved));

    await user.type(name, "{Enter}");

    expect(onRename).toHaveBeenLastCalledWith(
      saved,
      "Plan the week sooner",
      expect.anything(),
    );
  });

  it("saves a rename from the item as it was when the rename started", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename, showItem } = renderName(original);
    const name = await startRename(user);
    showItem({
      ...original,
      name: "Plan the month",
      updatedAt: "2026-11-22T10:00:00.000Z",
    });

    await user.type(name, " soon{Enter}");

    expect(onRename).toHaveBeenCalledWith(
      original,
      "Plan the week soon",
      expect.anything(),
    );
  });

  it("saves a rename from the current item when only its status changed since the rename started", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename, showItem } = renderName(original);
    const name = await startRename(user);
    const inProgress = {
      ...original,
      status: "in_progress" as const,
      updatedAt: "2026-11-22T10:00:00.000Z",
    };
    showItem(inProgress);

    await user.type(name, " soon{Enter}");

    expect(onRename).toHaveBeenCalledWith(
      inProgress,
      "Plan the week soon",
      expect.anything(),
    );
  });

  it("keeps the draft and shows the current name when the item changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename, showItem } = renderName(original);
    const name = await startRename(user);
    await user.type(name, " soon{Enter}");
    act(() => renameCallbacks(onRename).onChanged());

    showItem({
      ...original,
      name: "Plan the month",
      updatedAt: "2026-11-22T10:00:00.000Z",
    });

    expect(name).toHaveValue("Plan the week soon");
    expect(
      screen.getByText("Changed elsewhere. Current name: Plan the month"),
    ).toBeInTheDocument();
  });

  it("names no current name until a copy newer than the refused one arrives", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename, showItem } = renderName(original);
    const name = await startRename(user);
    showItem({
      ...original,
      status: "in_progress",
      updatedAt: "2026-11-22T10:00:00.000Z",
    });
    await user.type(name, " soon{Enter}");

    act(() => renameCallbacks(onRename).onChanged());

    expect(screen.getByText("Changed elsewhere.")).toBeInTheDocument();
    expect(screen.queryByText(/Current name/)).not.toBeInTheDocument();
  });

  it("saves again from the current item once the item changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename, showItem } = renderName(original);
    const name = await startRename(user);
    await user.type(name, " soon{Enter}");
    act(() => renameCallbacks(onRename).onChanged());
    const current = {
      ...original,
      name: "Plan the month",
      updatedAt: "2026-11-22T10:00:00.000Z",
    };
    showItem(current);

    await user.type(name, "{Enter}");

    expect(onRename).toHaveBeenLastCalledWith(
      current,
      "Plan the week soon",
      expect.anything(),
    );
  });

  it("does not save a blank name", async () => {
    const user = userEvent.setup();
    const { onRename } = renderName();
    const name = await startRename(user);

    await user.clear(name);
    await user.type(name, "   {Enter}");

    expect(onRename).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
  });

  it("cancels a rename with Escape", async () => {
    const user = userEvent.setup();
    const { onRename, onRenameCancel } = renderName();
    const name = await startRename(user);

    await user.type(name, " later{Escape}");

    expect(onRename).not.toHaveBeenCalled();
    expect(onRenameCancel).toHaveBeenCalled();
    expect(screen.getByText("Plan the week")).toBeInTheDocument();
  });

  it("cancels a rename with the Cancel button", async () => {
    const user = userEvent.setup();
    const { onRenameCancel } = renderName();
    await startRename(user);

    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onRenameCancel).toHaveBeenCalled();
    expect(
      screen.queryByRole("textbox", { name: "Name" }),
    ).not.toBeInTheDocument();
  });

  it("keeps the rename open while a change saves", async () => {
    const user = userEvent.setup();
    const { onRenameCancel, startSaving } = renderName();
    const name = await startRename(user);
    startSaving();

    await user.type(name, "{Escape}");

    expect(onRenameCancel).not.toHaveBeenCalled();
    expect(name).toBeInTheDocument();
  });

  it("shows Save and Cancel as disabled when disabled", async () => {
    const user = userEvent.setup();
    const { disable } = renderName();
    await startRename(user);

    disable();

    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeDisabled();
  });
});
