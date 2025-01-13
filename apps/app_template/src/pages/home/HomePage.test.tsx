import { describe, expect, test, render, screen } from "../../test";
import { HomePage } from "./HomePage";

describe("HomePage", async () => {
  test("renders", async () => {
    render(<HomePage />);

    expect(await screen.findByText(/Welcome/)).toBeInTheDocument();
  });
});
