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
  };
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
});
