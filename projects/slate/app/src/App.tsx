import { AppShell, Group, Title } from "@mantine/core";
import { ItemsPage } from "./pages/ItemsPage";

export function App() {
  return (
    <AppShell header={{ height: 56 }} padding="md">
      <AppShell.Header>
        <Group h="100%" px="lg">
          <Title order={1} fz="lg" fw={700}>
            Slate
          </Title>
        </Group>
      </AppShell.Header>
      <AppShell.Main bg="gray.0">
        <ItemsPage />
      </AppShell.Main>
    </AppShell>
  );
}
