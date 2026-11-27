import type { MenuItemConstructorOptions } from "electron";

interface MenuBarIcon<Menu> {
  setToolTip(toolTip: string): void;
  setTitle(title: string): void;
  popUpContextMenu(menu: Menu): void;
  on(event: "click", listener: (event: { ctrlKey?: boolean }) => void): unknown;
  on(event: "right-click", listener: () => void): unknown;
}

interface MenuBarOptions {
  profile: "dev" | "prod";
  open(): void;
  quit(): void;
}

export function showInMenuBar<Menu>(
  icon: MenuBarIcon<Menu>,
  buildMenu: (template: MenuItemConstructorOptions[]) => Menu,
  { profile, open, quit }: MenuBarOptions,
): void {
  // A template image has one colour, so the dev profile is told apart by a title instead of the
  // Dock icon's band.
  if (profile === "dev") {
    icon.setToolTip("Slate (dev)");
    icon.setTitle("DEV");
  } else {
    icon.setToolTip("Slate");
  }
  const menu = buildMenu([
    { label: "Open Slate", click: open },
    { type: "separator" },
    { label: "Quit Slate", click: quit },
  ]);

  icon.on("click", ({ ctrlKey }) => {
    if (ctrlKey) {
      icon.popUpContextMenu(menu);
    } else {
      open();
    }
  });
  icon.on("right-click", () => icon.popUpContextMenu(menu));
}
