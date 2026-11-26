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
import { List } from "@mantine/core";
import type { Item } from "../api";
import { item } from "../test/fakes";
import { ItemRow } from "./ItemRow";

type ItemRowProps = Parameters<typeof ItemRow>[0];

function renderRow(rowItem: Item = item({ name: "Plan the week" })) {
  const handlers = {
    onRename: vi.fn<ItemRowProps["onRename"]>(),
    onRenameCancel: vi.fn<ItemRowProps["onRenameCancel"]>(),
    onStatusChange: vi.fn<ItemRowProps["onStatusChange"]>(),
    onDelete: vi.fn<ItemRowProps["onDelete"]>(),
  };
  const row = (shown: Item, saving: boolean) => (
    <List>
      <ItemRow item={shown} saving={saving} {...handlers} />
    </List>
  );
  const { rerender } = render(row(rowItem, false));
  return {
    ...handlers,
    startSaving: () => rerender(row(rowItem, true)),
    showItem: (shown: Item) => rerender(row(shown, false)),
  };
}

function renameCallbacks(onRename: Mock<ItemRowProps["onRename"]>) {
  const [, , callbacks] = onRename.mock.lastCall!;
  return callbacks;
}

async function startRename(user: ReturnType<typeof userEvent.setup>) {
  await user.click(
    screen.getByRole("button", { name: "Rename Plan the week" }),
  );
  return screen.getByRole("textbox", { name: "Name" });
}

describe("ItemRow", () => {
  it("shows the item's name and status", () => {
    renderRow(item({ name: "Plan the week", status: "doing" }));

    expect(screen.getByText("Plan the week")).toBeInTheDocument();
    expect(
      screen.getByRole("combobox", { name: "Status of Plan the week" }),
    ).toHaveValue("doing");
  });

  it("strikes through a done item", () => {
    renderRow(item({ name: "Plan the week", status: "done" }));

    expect(screen.getByText("Plan the week")).toHaveStyle({
      textDecoration: "line-through",
    });
  });

  it("reports a status change", async () => {
    const user = userEvent.setup();
    const { onStatusChange } = renderRow();

    await user.selectOptions(
      screen.getByRole("combobox", { name: "Status of Plan the week" }),
      "Doing",
    );

    expect(onStatusChange).toHaveBeenCalledWith("doing");
  });

  it("keeps showing the saved status until the item changes", async () => {
    const user = userEvent.setup();
    renderRow();

    await user.selectOptions(
      screen.getByRole("combobox", { name: "Status of Plan the week" }),
      "Doing",
    );

    expect(
      screen.getByRole("combobox", { name: "Status of Plan the week" }),
    ).toHaveValue("todo");
  });

  it("closes the rename once the new name is saved", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename } = renderRow(original);
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
    renderRow();
    const name = await startRename(user);

    await user.type(name, " soon{Enter}");

    expect(screen.getByRole("textbox", { name: "Name" })).toHaveValue(
      "Plan the week soon",
    );
  });

  it("keeps newer edits when an earlier rename is saved", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename } = renderRow(original);
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
    const { onRename } = renderRow(original);
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
    const { onRename, showItem } = renderRow(original);
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
    const { onRename, showItem } = renderRow(original);
    const name = await startRename(user);
    const doing = {
      ...original,
      status: "doing" as const,
      updatedAt: "2026-11-22T10:00:00.000Z",
    };
    showItem(doing);

    await user.type(name, " soon{Enter}");

    expect(onRename).toHaveBeenCalledWith(
      doing,
      "Plan the week soon",
      expect.anything(),
    );
  });

  it("keeps the draft and shows the current name when the item changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename, showItem } = renderRow(original);
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

  it("names no current name until a newer copy of the item arrives", async () => {
    const user = userEvent.setup();
    const { onRename } = renderRow();
    const name = await startRename(user);
    await user.type(name, " soon{Enter}");

    act(() => renameCallbacks(onRename).onChanged());

    expect(screen.getByText("Changed elsewhere.")).toBeInTheDocument();
    expect(screen.queryByText(/Current name/)).not.toBeInTheDocument();
  });

  it("saves again from the current item once the item changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const { onRename, showItem } = renderRow(original);
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
    const { onRename } = renderRow();
    const name = await startRename(user);

    await user.clear(name);
    await user.type(name, "   {Enter}");

    expect(onRename).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
  });

  it("reports a delete", async () => {
    const user = userEvent.setup();
    const { onDelete } = renderRow();

    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    );

    expect(onDelete).toHaveBeenCalled();
  });

  it("does not offer a delete while a change saves", () => {
    const { startSaving } = renderRow();

    startSaving();

    expect(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    ).toBeDisabled();
  });

  it("does not change the status while a change saves", () => {
    const { startSaving } = renderRow();

    startSaving();

    expect(
      screen.getByRole("combobox", { name: "Status of Plan the week" }),
    ).toBeDisabled();
  });

  it("does not offer a move while a change saves", () => {
    const { startSaving } = renderRow();

    startSaving();

    expect(
      screen.getByRole("button", { name: "Move Plan the week" }),
    ).toHaveAttribute("aria-disabled", "true");
  });

  it("cancels a rename with Escape", async () => {
    const user = userEvent.setup();
    const { onRename, onRenameCancel } = renderRow();
    const name = await startRename(user);

    await user.type(name, " later{Escape}");

    expect(onRename).not.toHaveBeenCalled();
    expect(onRenameCancel).toHaveBeenCalled();
    expect(screen.getByText("Plan the week")).toBeInTheDocument();
  });

  it("cancels a rename with the Cancel button", async () => {
    const user = userEvent.setup();
    const { onRenameCancel } = renderRow();
    await startRename(user);

    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onRenameCancel).toHaveBeenCalled();
    expect(
      screen.queryByRole("textbox", { name: "Name" }),
    ).not.toBeInTheDocument();
  });

  it("keeps the rename open while a change saves", async () => {
    const user = userEvent.setup();
    const { onRenameCancel, startSaving } = renderRow();
    const name = await startRename(user);
    startSaving();

    await user.type(name, "{Escape}");

    expect(onRenameCancel).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeDisabled();
  });
});
