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

type OnAdd = (name: string, onSaved: () => void) => void;

describe("AddItemForm", () => {
  it("adds the typed name and clears it once saved", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn<OnAdd>((_name, onSaved) => onSaved());
    render(<AddItemForm saving={false} onAdd={onAdd} />);

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(onAdd).toHaveBeenCalledWith("Plan the week", expect.any(Function));
    expect(screen.getByRole("textbox", { name: "New item" })).toHaveValue("");
  });

  it("asks for a name to add with Enter and offers no Add button", () => {
    render(<AddItemForm saving={false} onAdd={vi.fn<OnAdd>()} />);

    expect(screen.getByRole("textbox", { name: "New item" })).toHaveAttribute(
      "placeholder",
      "Add an item and press Enter",
    );
    expect(
      screen.queryByRole("button", { name: "Add" }),
    ).not.toBeInTheDocument();
  });

  it("keeps newer typing when an earlier add is saved", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn<OnAdd>();
    render(<AddItemForm saving={false} onAdd={onAdd} />);
    const name = screen.getByRole("textbox", { name: "New item" });
    await user.type(name, "Plan the week{Enter}");
    await user.type(name, " ahead");
    const [[, onSaved]] = onAdd.mock.calls as [[string, () => void]];

    act(onSaved);

    expect(name).toHaveValue("Plan the week ahead");
  });

  it("keeps the typed name until it is saved", async () => {
    const user = userEvent.setup();
    render(<AddItemForm saving={false} onAdd={vi.fn<OnAdd>()} />);

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(screen.getByRole("textbox", { name: "New item" })).toHaveValue(
      "Plan the week",
    );
  });

  it("does not add a blank name", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn<OnAdd>();
    render(<AddItemForm saving={false} onAdd={onAdd} />);

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "   {Enter}",
    );

    expect(onAdd).not.toHaveBeenCalled();
  });

  it("does not add while a change saves", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn<OnAdd>();
    render(<AddItemForm saving onAdd={onAdd} />);

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(onAdd).not.toHaveBeenCalled();
  });
});
