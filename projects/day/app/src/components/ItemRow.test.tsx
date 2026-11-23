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
import { item } from "../test/fakes";
import { ItemRow } from "./ItemRow";

type ItemRowProps = Parameters<typeof ItemRow>[0];

function renderRow(props: Partial<ItemRowProps> = {}) {
  const handlers = {
    onRename: vi.fn<ItemRowProps["onRename"]>(),
    onRenameCancel: vi.fn<ItemRowProps["onRenameCancel"]>(),
    onStatusChange: vi.fn<ItemRowProps["onStatusChange"]>(),
  };
  const planTheWeek = item({ name: "Plan the week" });
  const row = (saving: boolean) => (
    <List>
      <ItemRow item={planTheWeek} saving={saving} {...handlers} {...props} />
    </List>
  );
  const { rerender } = render(row(false));
  return { ...handlers, startSaving: () => rerender(row(true)) };
}

describe("ItemRow", () => {
  it("closes the rename once the new name is saved", async () => {
    const { onRename } = renderRow();
    await userEvent.click(
      screen.getByRole("button", { name: "Rename Plan the week" }),
    );

    await userEvent.type(screen.getByLabelText("Name"), " soon{Enter}");
    onRename.mock.calls[0]![1]();

    expect(onRename).toHaveBeenCalledWith(
      "Plan the week soon",
      expect.any(Function),
    );
    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(screen.queryByLabelText("Name")).not.toBeInTheDocument();
  });

  it("cancels a rename with Escape", async () => {
    const { onRename, onRenameCancel } = renderRow();
    await userEvent.click(
      screen.getByRole("button", { name: "Rename Plan the week" }),
    );

    await userEvent.type(screen.getByLabelText("Name"), " later{Escape}");

    expect(onRename).not.toHaveBeenCalled();
    expect(onRenameCancel).toHaveBeenCalled();
    expect(screen.getByText("Plan the week")).toBeInTheDocument();
  });

  it("cancels a rename with the Cancel button", async () => {
    const { onRenameCancel } = renderRow();
    await userEvent.click(
      screen.getByRole("button", { name: "Rename Plan the week" }),
    );

    await userEvent.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onRenameCancel).toHaveBeenCalled();
    expect(screen.queryByLabelText("Name")).not.toBeInTheDocument();
  });

  it("keeps the rename open while a change saves", async () => {
    const { onRenameCancel, startSaving } = renderRow();
    await userEvent.click(
      screen.getByRole("button", { name: "Rename Plan the week" }),
    );
    startSaving();

    await userEvent.type(screen.getByLabelText("Name"), "{Escape}");

    expect(onRenameCancel).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeDisabled();
  });

  it("strikes through a done item", () => {
    renderRow({ item: item({ name: "Plan the week", status: "done" }) });

    expect(screen.getByText("Plan the week")).toHaveStyle({
      textDecoration: "line-through",
    });
  });

  it("reports a status change", async () => {
    const { onStatusChange } = renderRow();

    await userEvent.selectOptions(
      screen.getByLabelText("Status of Plan the week"),
      "Doing",
    );

    expect(onStatusChange).toHaveBeenCalledWith("doing");
  });
});
