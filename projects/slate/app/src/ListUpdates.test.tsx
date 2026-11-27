import {
  act,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  within,
} from "@lab/test-utils";
import { ListUpdates } from "./ListUpdates";
import { ItemsPage } from "./pages/ItemsPage";
import {
  type FakeSlateApi,
  fakeSlateApi,
  item,
  withSlateApi,
} from "./test/fakes";
import { layOutRowsVertically } from "./test/layout";

async function showList(api: FakeSlateApi) {
  render(
    <ListUpdates>
      <ItemsPage />
    </ListUpdates>,
    { wrapper: withSlateApi(api) },
  );
  await screen.findByRole("button", { name: "Open Plan the week" });
}

function changeElsewhere(api: FakeSlateApi) {
  api.listItems.mockResolvedValue([item({ name: "Book travel" })]);
  act(() => api.listChanged.change());
}

async function openItem(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByRole("button", { name: "Open Plan the week" }));
  return screen.getByRole("complementary", { name: "Item details" });
}

describe("ListUpdates", () => {
  it("shows a change made elsewhere as soon as the service announces it", async () => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);

    changeElsewhere(api);

    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("reloads the list on connecting, to show changes made while disconnected", async () => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    api.listItems.mockResolvedValue([item({ name: "Book travel" })]);

    act(() => api.listChanged.connect());

    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("holds a change while an item is dragged and shows it when the drag ends", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Write the report" }),
    ]);
    await showList(api);
    layOutRowsVertically();
    screen.getByRole("button", { name: "Move Plan the week" }).focus();
    await user.keyboard("[Space]");

    changeElsewhere(api);

    expect(api.listItems).toHaveBeenCalledOnce();
    await user.keyboard("[Escape]");
    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });

  it("keeps a rename's text when its item is deleted elsewhere, and shows the deletion when the rename ends", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    const panel = await openItem(user);
    await user.click(
      within(panel).getByRole("button", { name: "Rename Plan the week" }),
    );
    await user.type(within(panel).getByRole("textbox"), " today");

    api.listItems.mockResolvedValue([]);
    act(() => api.listChanged.change());

    expect(api.listItems).toHaveBeenCalledOnce();
    expect(within(panel).getByRole("textbox")).toHaveValue(
      "Plan the week today",
    );
    await user.keyboard("{Escape}");
    expect(
      await screen.findByText(/Nothing on your slate yet/),
    ).toBeInTheDocument();
  });

  it("holds a change while a description is edited and shows it when the edit ends", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    await showList(api);
    const panel = await openItem(user);
    await user.click(
      within(panel).getByRole("button", { name: "Add a description" }),
    );
    await user.type(within(panel).getByRole("textbox"), "For Monday");

    changeElsewhere(api);

    expect(api.listItems).toHaveBeenCalledOnce();
    await user.keyboard("{Escape}");
    expect(
      await screen.findByRole("button", { name: "Open Book travel" }),
    ).toBeInTheDocument();
  });
});
