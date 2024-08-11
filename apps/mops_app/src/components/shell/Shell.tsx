import { AppShell, Burger, Group, Title } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { Navbar } from "./Navbar";

export function Shell() {
  const [opened, { toggle }] = useDisclosure();

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
          <Title order={2}>Activities</Title>
        </Group>
      </AppShell.Header>
      <AppShell.Navbar>
        <Navbar opened={opened} onCloseClick={toggle} />
      </AppShell.Navbar>
      <AppShell.Main>Main content</AppShell.Main>
    </AppShell>
  );
}
