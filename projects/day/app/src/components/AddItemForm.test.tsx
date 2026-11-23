import {
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
    expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
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
    expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
  });
});
