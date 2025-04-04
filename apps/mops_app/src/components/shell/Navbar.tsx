import { Burger, Group, Title } from "@mantine/core";
import {
  IconCalendarDollar,
  IconLayoutBoard,
  IconLogout,
  IconSwitchHorizontal,
} from "@tabler/icons-react";
import classes from "./Navbar.module.css";
import { Link, useLocation } from "react-router-dom";
import logo from "./logo.png";
import { useStatsigClient } from "@statsig/react-bindings";

export const navigationItems = [
  { link: "/", label: "Chat", icon: IconLayoutBoard },
  // { link: "/plan", label: "Plan", icon: IconCalendarEvent },
  { link: "/spend", label: "Spend", icon: IconCalendarDollar },
  // { link: "/settings", label: "Settings", icon: IconSettings },
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

      <Footer />
    </nav>
  );
}

function Footer() {
  const { client } = useStatsigClient();

  if (!client.checkGate("iam")) return null;

  return (
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
  );
}
