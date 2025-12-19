import { describe, expect, render, screen, test } from "../../test";
import { SettingsPage } from "./SettingsPage";

describe("SettingsPage", async () => {
  test("renders", async () => {
    render(<SettingsPage />);

    expect(await screen.findByText(/Settings Page!/)).toBeInTheDocument();
  });
});
