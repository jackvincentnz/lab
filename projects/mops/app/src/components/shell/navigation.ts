import { IconCalendarDollar } from "@tabler/icons-react";

export const navigationItems = [
  { link: "/spend", label: "Spend", icon: IconCalendarDollar },
];

export function getNavigationLabel(pathname: string) {
  return navigationItems.find((item) => item.link === pathname)?.label;
}
