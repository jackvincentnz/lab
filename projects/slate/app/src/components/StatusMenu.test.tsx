import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
} from "@lab/test-utils";
import type { ItemStatus } from "../api";
import { StatusMenu } from "./StatusMenu";

function renderMenu(status: ItemStatus = "todo", saving = false) {
  const onChange = vi.fn<(status: ItemStatus) => void>();
  render(<StatusMenu status={status} saving={saving} onChange={onChange} />);
  return onChange;
}

describe("StatusMenu", () => {
  it("shows the current status", () => {
    renderMenu("doing");

    expect(
      screen.getByRole("button", { name: "Status: Doing" }),
    ).toBeInTheDocument();
  });

  it("offers every other status", async () => {
    const user = userEvent.setup();
    renderMenu("doing");

    await user.click(screen.getByRole("button", { name: "Status: Doing" }));

    expect(
      screen.getAllByRole("menuitem").map((option) => option.textContent),
    ).toEqual(["To do", "Done"]);
  });

  it("reports the status picked", async () => {
    const user = userEvent.setup();
    const onChange = renderMenu("todo");

    await user.click(screen.getByRole("button", { name: "Status: To do" }));
    await user.click(screen.getByRole("menuitem", { name: "Doing" }));

    expect(onChange).toHaveBeenCalledWith("doing");
  });

  it("does not change the status while a change saves", async () => {
    const user = userEvent.setup();
    renderMenu("todo", true);

    await user.click(screen.getByRole("button", { name: "Status: To do" }));

    expect(screen.getByRole("menuitem", { name: "Doing" })).toBeDisabled();
  });

  it("keeps the status button focusable while a change saves", () => {
    renderMenu("todo", true);

    expect(screen.getByRole("button", { name: "Status: To do" })).toBeEnabled();
  });
});
