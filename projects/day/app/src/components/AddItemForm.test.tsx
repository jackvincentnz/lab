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

describe("AddItemForm", () => {
  it("adds the typed name and clears it once saved", async () => {
    const onAdd = vi.fn<(name: string, onSaved: () => void) => void>();
    render(<AddItemForm saving={false} onAdd={onAdd} />);

    await userEvent.type(screen.getByLabelText("New item"), "Plan{Enter}");
    onAdd.mock.calls[0]![1]();

    expect(onAdd).toHaveBeenCalledWith("Plan", expect.any(Function));
    expect(await screen.findByLabelText("New item")).toHaveValue("");
  });

  it("does not add a blank name", async () => {
    render(<AddItemForm saving={false} onAdd={vi.fn()} />);

    await userEvent.type(screen.getByLabelText("New item"), "   ");

    expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
  });

  it("does not add while a change saves", async () => {
    render(<AddItemForm saving onAdd={vi.fn()} />);

    await userEvent.type(screen.getByLabelText("New item"), "Plan");

    expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
  });
});
