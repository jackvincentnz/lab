/**
 * Based on https://mantine.dev/app-shell/?e=AltLayout&s=code
 */
import { PropsWithChildren } from "react";
import { AppShell, Burger, Group, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";

// TODO: Move out to provider wrapper instead.
import "@mantine/core/styles.css";

import { NavbarLinks } from "./NavbarLinks";

import classes from "./Shell.module.css";

export interface AppShellProps {
  title: string;
}

export function Shell({ title, children }: PropsWithChildren<AppShellProps>) {
  const [opened, { toggle }] = useDisclosure();

  return (
    <AppShell
      layout="alt"
      header={{ height: 60 }}
      footer={{ height: 60 }}
      navbar={{ width: 300, breakpoint: "sm", collapsed: { mobile: !opened } }}
      aside={{
        width: 300,
        breakpoint: "md",
        collapsed: { desktop: false, mobile: true },
      }}
      padding="md"
    >
      <AppShell.Header>
        <Group h="100%" px="md">
          <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
          <Text>{title}</Text>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="md">
        <Group className={classes["nav-header"]}>
          <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
          <Text>Organizer</Text>
        </Group>

        <AppShell.Section grow>
          <NavbarLinks />
        </AppShell.Section>
      </AppShell.Navbar>

      <AppShell.Main>{children}</AppShell.Main>
    </AppShell>
  );
}
