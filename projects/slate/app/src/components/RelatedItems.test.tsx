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
import { RelatedItems } from "./RelatedItems";

type RelatedItemsProps = Parameters<typeof RelatedItems>[0];

function renderRelated(props: Partial<RelatedItemsProps> = {}) {
  const handlers = {
    onOpen: vi.fn<RelatedItemsProps["onOpen"]>(),
    onRelate: vi.fn<RelatedItemsProps["onRelate"]>(),
    onUnrelate: vi.fn<RelatedItemsProps["onUnrelate"]>(),
  };
  render(
    <RelatedItems
      related={[]}
      candidates={[]}
      openableIds={[]}
      disabled={false}
      {...handlers}
      {...props}
    />,
  );
  return handlers;
}

describe("RelatedItems", () => {
  it("opens a related item on the list", async () => {
    const user = userEvent.setup();
    const relatedItem: Item = item({
      name: "Book travel",
      status: "in_progress",
    });
    const { onOpen } = renderRelated({
      related: [relatedItem],
      openableIds: [relatedItem.id],
    });

    await user.click(screen.getByRole("button", { name: "Book travel" }));

    expect(screen.getByText("In progress")).toBeInTheDocument();
    expect(onOpen).toHaveBeenCalledWith(relatedItem.id);
  });

  it("names a related item that is not on the list without opening it", () => {
    renderRelated({ related: [item({ name: "Book travel", status: "done" })] });

    expect(screen.getByText("Book travel")).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Book travel" }),
    ).not.toBeInTheDocument();
  });

  it("unrelates an item", async () => {
    const user = userEvent.setup();
    const relatedItem = item({ name: "Book travel" });
    const { onUnrelate } = renderRelated({ related: [relatedItem] });

    await user.click(
      screen.getByRole("button", {
        name: "Remove Book travel from related items",
      }),
    );

    expect(onUnrelate).toHaveBeenCalledWith(relatedItem.id);
  });

  it("relates an item picked from the candidates", async () => {
    const user = userEvent.setup();
    const candidate = item({ name: "Plan the week" });
    const { onRelate } = renderRelated({ candidates: [candidate] });

    await user.click(
      screen.getByRole("combobox", { name: "Add a related item" }),
    );
    await user.click(
      await screen.findByRole("option", { name: "Plan the week" }),
    );

    expect(onRelate).toHaveBeenCalledWith(candidate.id);
  });

  it("relates the first match when Enter is pressed and clears the search", async () => {
    const user = userEvent.setup();
    const candidate = item({ name: "Plan the week" });
    const { onRelate } = renderRelated({
      candidates: [item({ name: "Book travel" }), candidate],
    });
    const picker = screen.getByRole("combobox", { name: "Add a related item" });

    await user.type(picker, "week[Enter]");

    expect(onRelate).toHaveBeenCalledWith(candidate.id);
    expect(picker).toHaveValue("");
  });

  it("changes nothing while disabled", () => {
    renderRelated({ related: [item({ name: "Book travel" })], disabled: true });

    expect(
      screen.getByRole("button", {
        name: "Remove Book travel from related items",
      }),
    ).toBeDisabled();
    expect(
      screen.getByRole("combobox", { name: "Add a related item" }),
    ).toBeDisabled();
  });
});
