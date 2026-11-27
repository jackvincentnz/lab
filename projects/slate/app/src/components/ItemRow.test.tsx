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
import { SortableItemRow } from "./ItemRow";

type ItemRowProps = Parameters<typeof SortableItemRow>[0];

function renderRow(
  rowItem: Item = item({ name: "Plan the week" }),
  open = false,
  rank = 1,
) {
  const handlers = {
    onOpen: vi.fn<ItemRowProps["onOpen"]>(),
    onStatusChange: vi.fn<ItemRowProps["onStatusChange"]>(),
  };
  const row = (disabled: boolean) => (
    <List>
      <SortableItemRow
        item={rowItem}
        rank={rank}
        disabled={disabled}
        open={open}
        {...handlers}
      />
    </List>
  );
  const { rerender } = render(row(false));
  return { ...handlers, disable: () => rerender(row(true)) };
}

describe("ItemRow", () => {
  it("shows the item's name", () => {
    renderRow();

    expect(screen.getByText("Plan the week")).toBeInTheDocument();
  });

  it("shows the item's rank", () => {
    renderRow(item({ name: "Plan the week" }), false, 3);

    expect(screen.getByText("#3")).toBeInTheDocument();
  });

  it("ticks and strikes through a done item", () => {
    renderRow(item({ name: "Plan the week", status: "done" }));

    expect(
      screen.getByRole("checkbox", { name: "Mark Plan the week done" }),
    ).toBeChecked();
    expect(screen.getByText("Plan the week")).toHaveStyle({
      textDecoration: "line-through",
    });
  });

  it("marks an item done when it is ticked", async () => {
    const user = userEvent.setup();
    const { onStatusChange } = renderRow();

    await user.click(
      screen.getByRole("checkbox", { name: "Mark Plan the week done" }),
    );

    expect(onStatusChange).toHaveBeenCalledWith("done");
  });

  it("marks a done item to do when it is unticked", async () => {
    const user = userEvent.setup();
    const { onStatusChange } = renderRow(
      item({ name: "Plan the week", status: "done" }),
    );

    await user.click(
      screen.getByRole("checkbox", { name: "Mark Plan the week done" }),
    );

    expect(onStatusChange).toHaveBeenCalledWith("todo");
  });

  it("keeps showing the saved status until the item changes", async () => {
    const user = userEvent.setup();
    renderRow();

    await user.click(
      screen.getByRole("checkbox", { name: "Mark Plan the week done" }),
    );

    expect(
      screen.getByRole("checkbox", { name: "Mark Plan the week done" }),
    ).not.toBeChecked();
  });

  it("opens the item when its name is clicked", async () => {
    const user = userEvent.setup();
    const { onOpen } = renderRow();

    await user.click(
      screen.getByRole("button", { name: "Open Plan the week" }),
    );

    expect(onOpen).toHaveBeenCalled();
  });

  it("opens the item when its rank is clicked", async () => {
    const user = userEvent.setup();
    const { onOpen } = renderRow(item({ name: "Plan the week" }), false, 3);

    await user.click(screen.getByText("#3"));

    expect(onOpen).toHaveBeenCalled();
  });

  it("says whether the item is open", () => {
    renderRow(item({ name: "Plan the week" }), true);

    expect(
      screen.getByRole("button", { name: "Open Plan the week" }),
    ).toHaveAttribute("aria-expanded", "true");
  });

  it("does not change the status when disabled", () => {
    const { disable } = renderRow();

    disable();

    expect(
      screen.getByRole("checkbox", { name: "Mark Plan the week done" }),
    ).toBeDisabled();
  });

  it("does not offer a move when disabled", () => {
    const { disable } = renderRow();

    disable();

    expect(
      screen.getByRole("button", { name: "Move Plan the week" }),
    ).toHaveAttribute("aria-disabled", "true");
  });
});
