import { AppShell, Group, Title } from "@mantine/core";
import { ItemsPage } from "./pages/ItemsPage";

export function App() {
  return (
    <AppShell header={{ height: 56 }} padding="md">
      <AppShell.Header>
        <Group h="100%" px="md">
          <Title order={1} size="h3">
            Slate
          </Title>
        </Group>
      </AppShell.Header>
      <AppShell.Main>
        <ItemsPage />
      </AppShell.Main>
    </AppShell>
  );
}
