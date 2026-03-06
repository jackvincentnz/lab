import {
  describe,
  expect,
  render,
  screen,
  setStatsigGate,
  test,
  userEvent,
  vi,
} from "../../test";
import { modals } from "@mantine/modals";
import { Navbar } from "./Navbar";

describe("Navbar", () => {
  test("marks the active navigation item", () => {
    render(<Navbar opened={false} onCloseClick={() => undefined} />, {
      route: "/spend",
    });

    expect(screen.getByRole("link", { name: /Spend/ })).toHaveAttribute(
      "data-active",
      "true",
    );
  });

  test("opens the reset data confirmation modal", async () => {
    const openConfirmModal = vi.spyOn(modals, "openConfirmModal");

    render(<Navbar opened={false} onCloseClick={() => undefined} />, {
      route: "/spend",
    });

    await userEvent.click(screen.getByRole("button", { name: "Reset Data" }));

    expect(openConfirmModal).toHaveBeenCalledWith(
      expect.objectContaining({
        title: "Reset Data",
      }),
    );

    openConfirmModal.mockRestore();
  });

  test("shows gated footer actions when the iam gate is enabled", () => {
    setStatsigGate("iam", true);

    render(<Navbar opened={false} onCloseClick={() => undefined} />, {
      route: "/spend",
    });

    expect(screen.getByText("Change account")).toBeVisible();
    expect(screen.getByText("Logout")).toBeVisible();
  });

  test("calls the reset endpoint after confirmation", async () => {
    const openConfirmModal = vi.spyOn(modals, "openConfirmModal");
    const fetchSpy = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue({ ok: true } as Response);

    render(<Navbar opened={false} onCloseClick={() => undefined} />, {
      route: "/spend",
    });

    await userEvent.click(screen.getByRole("button", { name: "Reset Data" }));

    const modalConfig = openConfirmModal.mock.calls[0]?.[0];
    modalConfig?.onConfirm?.();

    expect(fetchSpy).toHaveBeenCalledWith("/api/reset");

    openConfirmModal.mockRestore();
    fetchSpy.mockRestore();
  });
});
