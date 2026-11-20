import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
} from "@lab/test-utils";
import { Failure, Loading } from "./Status";

describe("Failure", () => {
  it("retries when asked", async () => {
    const retry = vi.fn();
    render(<Failure retry={retry} />);

    await userEvent.click(screen.getByRole("button", { name: "Try again" }));

    expect(screen.getByRole("alert")).toHaveTextContent(
      "Couldn’t load this view",
    );
    expect(retry).toHaveBeenCalledOnce();
  });
});

describe("Loading", () => {
  it("announces loading", () => {
    render(<Loading />);

    expect(screen.getByRole("status")).toHaveTextContent("Loading…");
  });
});
