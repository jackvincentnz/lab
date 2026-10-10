import {
  afterEach,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
} from "../../../../../tools/bazel/vitest/test-utils";
import { NavbarLinks } from "./NavbarLinks";

describe("NavbarLinks", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/");
  });

  it("links to each app", () => {
    render(<NavbarLinks />);

    expect(screen.getByRole("link", { name: "Tasks" })).toHaveAttribute(
      "href",
      "/task",
    );
    expect(screen.getByRole("link", { name: "Journal" })).toHaveAttribute(
      "href",
      "/journal",
    );
  });

  it("marks the link for the current page as active", () => {
    window.history.replaceState(null, "", "/journal/today");

    render(<NavbarLinks />);

    expect(screen.getByRole("link", { name: "Journal" })).toHaveAttribute(
      "data-active",
    );
    expect(screen.getByRole("link", { name: "Tasks" })).not.toHaveAttribute(
      "data-active",
    );
  });

  it("marks a clicked link as active", async () => {
    const user = userEvent.setup();
    window.history.replaceState(null, "", "/task");
    render(<NavbarLinks />);

    await user.click(screen.getByRole("link", { name: "Journal" }));

    expect(screen.getByRole("link", { name: "Journal" })).toHaveAttribute(
      "data-active",
    );
    expect(screen.getByRole("link", { name: "Tasks" })).not.toHaveAttribute(
      "data-active",
    );
  });
});
