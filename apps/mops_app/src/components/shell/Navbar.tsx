import { useState } from "react";
import { Group, Burger, Title } from "@mantine/core";
import {
  IconBellRinging,
  IconCalendarEvent,
  IconFingerprint,
  IconKey,
  IconSettings,
  Icon2fa,
  IconDatabaseImport,
  IconReceipt2,
  IconSwitchHorizontal,
  IconLogout,
} from "@tabler/icons-react";
import classes from "./Navbar.module.css";

const data = [
  { link: "", label: "Activities", icon: IconCalendarEvent },
  { link: "", label: "Notifications", icon: IconBellRinging },
  { link: "", label: "Billing", icon: IconReceipt2 },
  { link: "", label: "Security", icon: IconFingerprint },
  { link: "", label: "SSH Keys", icon: IconKey },
  { link: "", label: "Databases", icon: IconDatabaseImport },
  { link: "", label: "Authentication", icon: Icon2fa },
  { link: "", label: "Other Settings", icon: IconSettings },
];

export interface NavbarProps {
  opened: boolean;
  onCloseClick: () => void;
}

export function Navbar({ opened, onCloseClick }: NavbarProps) {
  const [active, setActive] = useState("Billing");

  const links = data.map((item) => (
    <a
      className={classes["link"]}
      data-active={item.label === active || undefined}
      href={item.link}
      key={item.label}
      onClick={(event) => {
        event.preventDefault();
        setActive(item.label);
      }}
    >
      <item.icon className={classes["linkIcon"]} stroke={1.5} />
      <span>{item.label}</span>
    </a>
  ));

  return (
    <nav className={classes["navbar"]}>
      <div className={classes["navbarMain"]}>
        <Group className={classes["header"]} justify="left">
          <Burger
            opened={opened}
            onClick={onCloseClick}
            hiddenFrom="sm"
            size="sm"
            color="white"
          />
          <Title order={2}>Mops</Title>
        </Group>
        {links}
      </div>

      <div className={classes["footer"]}>
        <a
          href="#"
          className={classes["link"]}
          onClick={(event) => event.preventDefault()}
        >
          <IconSwitchHorizontal className={classes["linkIcon"]} stroke={1.5} />
          <span>Change account</span>
        </a>

        <a
          href="#"
          className={classes["link"]}
          onClick={(event) => event.preventDefault()}
        >
          <IconLogout className={classes["linkIcon"]} stroke={1.5} />
          <span>Logout</span>
        </a>
      </div>
    </nav>
  );
}
