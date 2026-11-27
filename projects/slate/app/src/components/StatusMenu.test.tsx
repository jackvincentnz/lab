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

function renderMenu(status: ItemStatus = "todo", disabled = false) {
  const onChange = vi.fn<(status: ItemStatus) => void>();
  render(
    <StatusMenu status={status} disabled={disabled} onChange={onChange} />,
  );
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
    ).toEqual(["To do", "Done", "Deferred", "Won't do", "Delegated"]);
  });

  it("reports the status picked", async () => {
    const user = userEvent.setup();
    const onChange = renderMenu("todo");

    await user.click(screen.getByRole("button", { name: "Status: To do" }));
    await user.click(screen.getByRole("menuitem", { name: "Doing" }));

    expect(onChange).toHaveBeenCalledWith("doing");
  });

  it("reports a status that takes the item off the list", async () => {
    const user = userEvent.setup();
    const onChange = renderMenu("todo");

    await user.click(screen.getByRole("button", { name: "Status: To do" }));
    await user.click(screen.getByRole("menuitem", { name: "Won't do" }));

    expect(onChange).toHaveBeenCalledWith("wont_do");
  });

  it("shows a status that takes the item off the list", () => {
    renderMenu("delegated");

    expect(
      screen.getByRole("button", { name: "Status: Delegated" }),
    ).toBeInTheDocument();
  });

  it("does not change the status when disabled", async () => {
    const user = userEvent.setup();
    renderMenu("todo", true);

    await user.click(screen.getByRole("button", { name: "Status: To do" }));

    expect(screen.getByRole("menuitem", { name: "Doing" })).toBeDisabled();
  });

  it("keeps the status button focusable when disabled", () => {
    renderMenu("todo", true);

    expect(screen.getByRole("button", { name: "Status: To do" })).toBeEnabled();
  });
});
