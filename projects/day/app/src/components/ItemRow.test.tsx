import {
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

function renderRow(planTheWeek: Item = item({ name: "Plan the week" })) {
  const handlers = {
    onRename: vi.fn<ItemRowProps["onRename"]>(),
    onRenameCancel: vi.fn<ItemRowProps["onRenameCancel"]>(),
    onStatusChange: vi.fn<ItemRowProps["onStatusChange"]>(),
  };
  const row = (saving: boolean) => (
    <List>
      <ItemRow item={planTheWeek} saving={saving} {...handlers} />
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

  it("reports a status change and keeps showing the saved status", async () => {
    const user = userEvent.setup();
    const { onStatusChange } = renderRow();

    await user.selectOptions(
      screen.getByRole("combobox", { name: "Status of Plan the week" }),
      "Doing",
    );

    expect(onStatusChange).toHaveBeenCalledWith("doing");
    expect(
      screen.getByRole("combobox", { name: "Status of Plan the week" }),
    ).toHaveValue("todo");
  });

  it("closes the rename once the new name is saved", async () => {
    const user = userEvent.setup();
    const { onRename } = renderRow();
    onRename.mockImplementation((_name, onSaved) => onSaved());

    await user.type(await startRename(user), " soon{Enter}");

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

    await user.type(await startRename(user), " soon{Enter}");

    expect(screen.getByRole("textbox", { name: "Name" })).toHaveValue(
      "Plan the week soon",
    );
  });

  it("cancels a rename with Escape", async () => {
    const user = userEvent.setup();
    const { onRename, onRenameCancel } = renderRow();

    await user.type(await startRename(user), " later{Escape}");

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
