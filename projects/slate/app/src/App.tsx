import { Container, Stack, Title } from "@mantine/core";
import { ItemsPage } from "./pages/ItemsPage";

export function App() {
  return (
    <Container size="sm" py="xl">
      <Stack>
        <Title order={1}>Slate</Title>
        <ItemsPage />
      </Stack>
    </Container>
  );
}
