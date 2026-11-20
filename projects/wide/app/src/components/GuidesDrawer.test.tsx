import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
} from "@lab/test-utils";
import { GuidesDrawer } from "./GuidesDrawer";

describe("GuidesDrawer", () => {
  it("shows the shared refinement guides when opened", async () => {
    render(<GuidesDrawer opened onClose={vi.fn()} />);

    expect(
      await screen.findByRole("heading", { name: "Problem refinement guide" }),
    ).toBeVisible();
    expect(
      screen.getByRole("heading", {
        name: "Candidate solution refinement guide",
      }),
    ).toBeVisible();
  });

  it("asks to close on Escape", async () => {
    const onClose = vi.fn();
    render(<GuidesDrawer opened onClose={onClose} />);
    await screen.findByRole("dialog");

    await userEvent.keyboard("{Escape}");

    expect(onClose).toHaveBeenCalledOnce();
  });

  it("renders nothing while closed", () => {
    render(<GuidesDrawer opened={false} onClose={vi.fn()} />);

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });
});
