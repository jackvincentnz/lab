import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
  within,
} from "@lab/test-utils";
import { item } from "../test/fakes";
import { ItemList } from "./ItemList";

type ItemListProps = Parameters<typeof ItemList>[0];

describe("ItemList", () => {
  it("shows the items in the order given", () => {
    render(
      <ItemList
        items={[item({ name: "Plan the week" }), item({ name: "Book travel" })]}
        saving={false}
        onRename={vi.fn()}
        onRenameCancel={vi.fn()}
        onStatusChange={vi.fn()}
      />,
    );

    const rows = within(
      screen.getByRole("list", { name: "Items" }),
    ).getAllByRole("listitem");
    expect(rows.map((row) => row.textContent)).toEqual([
      expect.stringContaining("Plan the week"),
      expect.stringContaining("Book travel"),
    ]);
  });

  it("reports which item a change is for", async () => {
    const user = userEvent.setup();
    const bookTravel = item({ name: "Book travel" });
    const onStatusChange = vi.fn<ItemListProps["onStatusChange"]>();
    const onRename = vi.fn<ItemListProps["onRename"]>();
    render(
      <ItemList
        items={[item({ name: "Plan the week" }), bookTravel]}
        saving={false}
        onRename={onRename}
        onRenameCancel={vi.fn()}
        onStatusChange={onStatusChange}
      />,
    );

    await user.selectOptions(
      screen.getByRole("combobox", { name: "Status of Book travel" }),
      "Done",
    );
    await user.click(
      screen.getByRole("button", { name: "Rename Book travel" }),
    );
    await user.type(screen.getByRole("textbox", { name: "Name" }), "s{Enter}");

    expect(onStatusChange).toHaveBeenCalledWith(bookTravel, "done");
    expect(onRename).toHaveBeenCalledWith(
      bookTravel,
      "Book travels",
      expect.any(Function),
    );
  });
});
