import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
} from "@lab/test-utils";
import { Screen } from "./Screen";

describe("Screen", () => {
  it("says it is syncing before the first result", () => {
    render(
      <Screen crumbs={["Signals"]} refresh={vi.fn()} syncedAt={null} loading>
        Body
      </Screen>,
    );

    expect(screen.getByText("Syncing…")).toBeVisible();
  });

  it("shows the sync time and refreshes on request", async () => {
    const refresh = vi.fn();
    render(
      <Screen
        crumbs={["Signals", "Detail"]}
        refresh={refresh}
        syncedAt={new Date(2026, 0, 1, 9, 5)}
        loading={false}
      >
        Body
      </Screen>,
    );

    await userEvent.click(screen.getByRole("button", { name: "Refresh" }));

    expect(screen.getByText(/^Synced /)).toBeVisible();
    expect(
      screen.getByRole("navigation", { name: "Breadcrumb" }),
    ).toHaveTextContent("SignalsDetail");
    expect(refresh).toHaveBeenCalledOnce();
  });
});
