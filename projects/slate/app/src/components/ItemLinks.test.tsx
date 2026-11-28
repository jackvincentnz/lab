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
import { ItemLinks } from "./ItemLinks";

type ItemLinksProps = Parameters<typeof ItemLinks>[0];

function renderLinks(props: Partial<ItemLinksProps> = {}) {
  const handlers = {
    onOpen: vi.fn<ItemLinksProps["onOpen"]>(),
    onLink: vi.fn<ItemLinksProps["onLink"]>(),
    onUnlink: vi.fn<ItemLinksProps["onUnlink"]>(),
  };
  render(
    <ItemLinks
      links={[]}
      candidates={[]}
      openableIds={[]}
      disabled={false}
      {...handlers}
      {...props}
    />,
  );
  return handlers;
}

describe("ItemLinks", () => {
  it("opens a linked item on the list", async () => {
    const user = userEvent.setup();
    const linked: Item = item({ name: "Book travel", status: "in_progress" });
    const { onOpen } = renderLinks({
      links: [linked],
      openableIds: [linked.id],
    });

    await user.click(screen.getByRole("button", { name: "Book travel" }));

    expect(screen.getByText("In progress")).toBeInTheDocument();
    expect(onOpen).toHaveBeenCalledWith(linked.id);
  });

  it("names a linked item that is not on the list without opening it", () => {
    renderLinks({ links: [item({ name: "Book travel", status: "done" })] });

    expect(screen.getByText("Book travel")).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Book travel" }),
    ).not.toBeInTheDocument();
  });

  it("unlinks an item", async () => {
    const user = userEvent.setup();
    const linked = item({ name: "Book travel" });
    const { onUnlink } = renderLinks({ links: [linked] });

    await user.click(
      screen.getByRole("button", { name: "Unlink Book travel" }),
    );

    expect(onUnlink).toHaveBeenCalledWith(linked.id);
  });

  it("links an item picked from the candidates", async () => {
    const user = userEvent.setup();
    const candidate = item({ name: "Plan the week" });
    const { onLink } = renderLinks({ candidates: [candidate] });

    await user.click(screen.getByRole("combobox", { name: "Link an item" }));
    await user.click(
      await screen.findByRole("option", { name: "Plan the week" }),
    );

    expect(onLink).toHaveBeenCalledWith(candidate.id);
  });

  it("links the first match when Enter is pressed and clears the search", async () => {
    const user = userEvent.setup();
    const candidate = item({ name: "Plan the week" });
    const { onLink } = renderLinks({
      candidates: [item({ name: "Book travel" }), candidate],
    });
    const picker = screen.getByRole("combobox", { name: "Link an item" });

    await user.type(picker, "week[Enter]");

    expect(onLink).toHaveBeenCalledWith(candidate.id);
    expect(picker).toHaveValue("");
  });

  it("changes nothing while disabled", () => {
    renderLinks({ links: [item({ name: "Book travel" })], disabled: true });

    expect(
      screen.getByRole("button", { name: "Unlink Book travel" }),
    ).toBeDisabled();
    expect(
      screen.getByRole("combobox", { name: "Link an item" }),
    ).toBeDisabled();
  });
});
