/**
 * Adapted from https://github.com/mantinedev/ui.mantine.dev/blob/master/lib/NavbarSimple/NavbarSimple.tsx
 */
import { useState } from "react";
import { IconListCheck, IconNotebook } from "@tabler/icons-react";

import classes from "./NavbarLinks.module.css";

const data = [
  {
    icon: IconListCheck,
    label: "Tasks",
    link: "/task",
  },
  {
    icon: IconNotebook,
    label: "Journal",
    link: "/journal",
  },
];

export function NavbarLinks() {
  const [active, setActive] = useState(getInitialLinkLabel());

  return data.map((item) => (
    <a
      className={classes["link"]}
      data-active={item.label === active || undefined}
      href={item.link}
      key={item.label}
      onClick={() => {
        setActive(item.label);
      }}
    >
      <item.icon className={classes["linkIcon"]} stroke={1.5} />
      <span>{item.label}</span>
    </a>
  ));
}

function getInitialLinkLabel() {
  return data.filter((link) => window.location.href.includes(link.link))[0]
    ?.label;
}
