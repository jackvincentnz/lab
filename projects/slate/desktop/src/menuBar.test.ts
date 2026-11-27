import { EventEmitter } from "node:events";
import type { MenuItemConstructorOptions } from "electron";
import { describe, expect, it, vi } from "vitest";
import { showInMenuBar } from "./menuBar.js";

function fakeMenuBar({ profile = "prod" }: { profile?: "prod" | "dev" } = {}) {
  const icon = Object.assign(new EventEmitter(), {
    setToolTip: vi.fn<(toolTip: string) => void>(),
    setTitle: vi.fn<(title: string) => void>(),
    popUpContextMenu: vi.fn<(menu: MenuItemConstructorOptions[]) => void>(),
  });
  const actions = { open: vi.fn(), quit: vi.fn() };

  showInMenuBar(icon, (template) => template, { profile, ...actions });

  return { icon, ...actions };
}

function contextMenu(
  icon: ReturnType<typeof fakeMenuBar>["icon"],
): MenuItemConstructorOptions[] {
  icon.emit("right-click", {});
  return icon.popUpContextMenu.mock.lastCall?.[0] ?? [];
}

function clickItem(
  icon: ReturnType<typeof fakeMenuBar>["icon"],
  label: string,
): void {
  const item = contextMenu(icon).find((option) => option.label === label);
  (item?.click as (() => void) | undefined)?.();
}

describe("showInMenuBar", () => {
  it("opens Slate when the icon is clicked", () => {
    const { icon, open } = fakeMenuBar();

    icon.emit("click", {});

    expect(open).toHaveBeenCalled();
    expect(icon.popUpContextMenu).not.toHaveBeenCalled();
  });

  it("shows the menu when the icon is right-clicked", () => {
    const { icon, open } = fakeMenuBar();

    icon.emit("right-click", {});

    expect(icon.popUpContextMenu).toHaveBeenCalled();
    expect(open).not.toHaveBeenCalled();
  });

  it("shows the menu when the icon is clicked with Control held", () => {
    const { icon, open } = fakeMenuBar();

    icon.emit("click", { ctrlKey: true });

    expect(icon.popUpContextMenu).toHaveBeenCalled();
    expect(open).not.toHaveBeenCalled();
  });

  it("lists Open Slate and Quit Slate in the menu", () => {
    const { icon } = fakeMenuBar();

    const labels = contextMenu(icon).map((item) => item.label ?? item.type);

    expect(labels).toEqual(["Open Slate", "separator", "Quit Slate"]);
  });

  it("opens Slate from the menu", () => {
    const { icon, open } = fakeMenuBar();

    clickItem(icon, "Open Slate");

    expect(open).toHaveBeenCalled();
  });

  it("quits Slate from the menu", () => {
    const { icon, quit } = fakeMenuBar();

    clickItem(icon, "Quit Slate");

    expect(quit).toHaveBeenCalled();
  });

  it("names Slate in the tooltip and shows no title for the prod profile", () => {
    const { icon } = fakeMenuBar({ profile: "prod" });

    expect(icon.setToolTip).toHaveBeenCalledWith("Slate");
    expect(icon.setTitle).not.toHaveBeenCalled();
  });

  it("marks the dev profile with a DEV title beside the icon", () => {
    const { icon } = fakeMenuBar({ profile: "dev" });

    expect(icon.setToolTip).toHaveBeenCalledWith("Slate (dev)");
    expect(icon.setTitle).toHaveBeenCalledWith("DEV");
  });
});
