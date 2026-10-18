import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  within,
} from "@lab/test-utils";
import { Shell } from "./Shell";

describe("Shell", () => {
  it("renders the title, navigation and content", () => {
    render(<Shell title="Page title">Page content</Shell>);

    expect(
      within(screen.getByRole("banner")).getByText("Page title"),
    ).toBeInTheDocument();
    expect(
      within(screen.getByRole("navigation")).getByText("Page title"),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Tasks" })).toBeInTheDocument();
    expect(screen.getByRole("main")).toHaveTextContent("Page content");
  });

  it("toggles both menu buttons together", async () => {
    const user = userEvent.setup();
    render(<Shell title="Page title" />);
    const [headerBurger, navbarBurger] = screen.getAllByRole("button");
    // Mantine marks the open state on the burger icon inside each button.
    const isOpened = (button: HTMLElement) =>
      button.firstElementChild?.hasAttribute("data-opened");

    await user.click(headerBurger);

    expect(isOpened(headerBurger)).toBe(true);
    expect(isOpened(navbarBurger)).toBe(true);

    await user.click(navbarBurger);

    expect(isOpened(headerBurger)).toBe(false);
    expect(isOpened(navbarBurger)).toBe(false);
  });
});
