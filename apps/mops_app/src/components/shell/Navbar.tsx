import { Burger, Group, Title } from "@mantine/core";
import {
  IconCalendarEvent,
  IconLogout,
  IconSettings,
  IconSwitchHorizontal,
  IconLayoutBoard,
} from "@tabler/icons-react";
import classes from "./Navbar.module.css";
import { Link, useLocation } from "react-router-dom";
import logo from "./logo.png";

export const navigationItems = [
  { link: "/", label: "Home", icon: IconLayoutBoard },
  { link: "/activities", label: "Activities", icon: IconCalendarEvent },
  { link: "/settings", label: "Settings", icon: IconSettings },
];

export interface NavbarProps {
  opened: boolean;
  onCloseClick: () => void;
}

export function Navbar({ opened, onCloseClick }: NavbarProps) {
  const location = useLocation();

  const links = navigationItems.map((item) => (
    <Link
      to={item.link}
      className={classes["link"]}
      data-active={item.link === location.pathname || undefined}
      key={item.label}
    >
      <item.icon className={classes["linkIcon"]} stroke={1.5} />
      <span>{item.label}</span>
    </Link>
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
          <img className={classes["logo"]} src={logo} alt="logo" />
          <Title order={2}> Mops</Title>
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
