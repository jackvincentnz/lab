import {
  act,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
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
  const row = (saving: boolean) => (
    <List>
      <ItemRow item={rowItem} saving={saving} {...handlers} />
    </List>
  );
  const { rerender } = render(row(false));
  return { ...handlers, startSaving: () => rerender(row(true)) };
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
    const { onRename } = renderRow();
    onRename.mockImplementation((_name, onSaved) => onSaved());
    const name = await startRename(user);

    await user.type(name, " soon{Enter}");

    expect(onRename).toHaveBeenCalledWith(
      "Plan the week soon",
      expect.any(Function),
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
    const { onRename } = renderRow();
    const name = await startRename(user);
    await user.type(name, " soon{Enter}");
    await user.type(name, "er");
    const [[, onSaved]] = onRename.mock.calls as [[string, () => void]];

    act(onSaved);

    expect(screen.getByRole("textbox", { name: "Name" })).toHaveValue(
      "Plan the week sooner",
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
