import { PropsWithChildren } from "react";
import { AppShell, Burger, Group, Title } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { Navbar, navigationItems } from "./Navbar";
import { useLocation } from "react-router-dom";

export function Shell({ children }: PropsWithChildren) {
  const [opened, { toggle }] = useDisclosure();
  const location = useLocation();

  const pageTitle = navigationItems.find(
    (item) => item.link === location.pathname,
  );

  return (
    <AppShell
      layout="alt"
      header={{ height: 60 }}
      navbar={{ width: 300, breakpoint: "sm", collapsed: { mobile: !opened } }}
      padding="md"
    >
      <AppShell.Header>
        <Group h="100%" px="md">
          <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
          <Title order={2}>{pageTitle?.label}</Title>
        </Group>
      </AppShell.Header>
      <AppShell.Navbar>
        <Navbar opened={opened} onCloseClick={toggle} />
      </AppShell.Navbar>
      <AppShell.Main>{children}</AppShell.Main>
    </AppShell>
  );
}
