import { Container, Stack, Title } from "@mantine/core";
import { ItemList } from "./ItemList";

export function App() {
  return (
    <Container size="sm" py="xl">
      <Stack>
        <Title order={1}>Day</Title>
        <ItemList />
      </Stack>
    </Container>
  );
}
