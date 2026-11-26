import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
} from "@lab/test-utils";
import type { Item } from "../api";
import { item } from "../test/fakes";
import { ItemPanel } from "./ItemPanel";

type ItemPanelProps = Parameters<typeof ItemPanel>[0];

function renderPanel(panelItem: Item = item({ name: "Plan the week" })) {
  const handlers = {
    onClose: vi.fn<ItemPanelProps["onClose"]>(),
    onRename: vi.fn<ItemPanelProps["onRename"]>(),
    onRenameCancel: vi.fn<ItemPanelProps["onRenameCancel"]>(),
    onStatusChange: vi.fn<ItemPanelProps["onStatusChange"]>(),
    onDelete: vi.fn<ItemPanelProps["onDelete"]>(),
  };
  const panel = (shown: Item, saving: boolean) => (
    <ItemPanel item={shown} saving={saving} {...handlers} />
  );
  const { rerender } = render(panel(panelItem, false));
  return {
    ...handlers,
    startSaving: () => rerender(panel(panelItem, true)),
    showItem: (shown: Item) => rerender(panel(shown, false)),
  };
}

describe("ItemPanel", () => {
  it("shows the item's name and status", () => {
    renderPanel(item({ name: "Plan the week", status: "doing" }));

    expect(
      screen.getByRole("heading", { name: "Plan the week" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Status: Doing" }),
    ).toBeInTheDocument();
  });

  it("reports a status change", async () => {
    const user = userEvent.setup();
    const { onStatusChange } = renderPanel();

    await user.click(screen.getByRole("button", { name: "Status: To do" }));
    await user.click(screen.getByRole("menuitem", { name: "Done" }));

    expect(onStatusChange).toHaveBeenCalledWith("done");
  });

  it("closes with the close button", async () => {
    const user = userEvent.setup();
    const { onClose } = renderPanel();

    await user.click(screen.getByRole("button", { name: "Close details" }));

    expect(onClose).toHaveBeenCalled();
  });

  it("takes focus when it opens", () => {
    renderPanel();

    expect(
      screen.getByRole("complementary", { name: "Item details" }),
    ).toHaveFocus();
  });

  it("closes with Escape", async () => {
    const user = userEvent.setup();
    const { onClose } = renderPanel();

    await user.keyboard("{Escape}");

    expect(onClose).toHaveBeenCalled();
  });

  it("stays open when Escape cancels a rename", async () => {
    const user = userEvent.setup();
    const { onClose, onRenameCancel } = renderPanel();
    await user.click(
      screen.getByRole("button", { name: "Rename Plan the week" }),
    );

    await user.type(screen.getByRole("textbox", { name: "Name" }), "{Escape}");

    expect(onRenameCancel).toHaveBeenCalled();
    expect(onClose).not.toHaveBeenCalled();
  });

  it("stays open when Escape closes the status menu", async () => {
    const user = userEvent.setup();
    const { onClose } = renderPanel();
    await user.click(screen.getByRole("button", { name: "Status: To do" }));

    await user.keyboard("{Escape}");

    expect(screen.queryByRole("menu")).not.toBeInTheDocument();
    expect(onClose).not.toHaveBeenCalled();
  });

  it("starts with no rename open when it shows another item", async () => {
    const user = userEvent.setup();
    const { showItem } = renderPanel();
    await user.click(
      screen.getByRole("button", { name: "Rename Plan the week" }),
    );

    showItem(item({ name: "Book travel" }));

    expect(
      screen.queryByRole("textbox", { name: "Name" }),
    ).not.toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "Book travel" }),
    ).toBeInTheDocument();
  });

  it("reports a delete", async () => {
    const user = userEvent.setup();
    const { onDelete } = renderPanel();

    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    );

    expect(onDelete).toHaveBeenCalled();
  });

  it("does not offer a delete while a change saves", () => {
    const { startSaving } = renderPanel();

    startSaving();

    expect(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    ).toBeDisabled();
  });
});
