import { AppShell, Group, Image, Title } from "@mantine/core";
// Inlined, it would be a data: URL, which the service's content security policy blocks.
import mark from "../../icons/slate-mark.svg?no-inline";
import { ListUpdates } from "./ListUpdates";
import { ItemsPage } from "./pages/ItemsPage";

export function App() {
  return (
    <AppShell header={{ height: 56 }}>
      <AppShell.Header>
        <Group h="100%" px="lg" gap="xs">
          <Image src={mark} alt="" w={28} h={28} />
          <Title order={1} fz="lg" fw={700}>
            Slate
          </Title>
        </Group>
      </AppShell.Header>
      <AppShell.Main bg="gray.0">
        <ListUpdates>
          <ItemsPage />
        </ListUpdates>
      </AppShell.Main>
    </AppShell>
  );
}
