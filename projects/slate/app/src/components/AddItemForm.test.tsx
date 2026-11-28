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
import { AddItemForm } from "./AddItemForm";

type OnAdd = (name: string, onFailed: () => void) => void;

function failAdd(onAdd: ReturnType<typeof vi.fn<OnAdd>>) {
  const [[, onFailed]] = onAdd.mock.calls as [[string, () => void]];
  act(onFailed);
}

describe("AddItemForm", () => {
  it("adds the typed name and clears it", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn<OnAdd>();
    render(<AddItemForm onAdd={onAdd} />);

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(onAdd).toHaveBeenCalledWith("Plan the week", expect.any(Function));
    expect(screen.getByRole("textbox", { name: "New item" })).toHaveValue("");
  });

  it("asks for a name to add with Enter and offers no Add button", () => {
    render(<AddItemForm onAdd={vi.fn<OnAdd>()} />);

    expect(screen.getByRole("textbox", { name: "New item" })).toHaveAttribute(
      "placeholder",
      "Add an item and press Enter",
    );
    expect(
      screen.queryByRole("button", { name: "Add" }),
    ).not.toBeInTheDocument();
  });

  it("adds names typed one after another without joining them", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn<OnAdd>();
    render(<AddItemForm onAdd={onAdd} />);

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}Book travel{Enter}",
    );

    expect(onAdd.mock.calls.map(([name]) => name)).toEqual([
      "Plan the week",
      "Book travel",
    ]);
  });

  it("puts the name back when its add fails", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn<OnAdd>();
    render(<AddItemForm onAdd={onAdd} />);
    const name = screen.getByRole("textbox", { name: "New item" });
    await user.type(name, "Plan the week{Enter}");

    failAdd(onAdd);

    expect(name).toHaveValue("Plan the week");
  });

  it("keeps newer typing when an earlier add fails", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn<OnAdd>();
    render(<AddItemForm onAdd={onAdd} />);
    const name = screen.getByRole("textbox", { name: "New item" });
    await user.type(name, "Plan the week{Enter}Book");

    failAdd(onAdd);

    expect(name).toHaveValue("Book");
  });

  it("does not add a blank name", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn<OnAdd>();
    render(<AddItemForm onAdd={onAdd} />);

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "   {Enter}",
    );

    expect(onAdd).not.toHaveBeenCalled();
  });
});
