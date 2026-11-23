import {
  Alert,
  Button,
  Center,
  List,
  Loader,
  Stack,
  Text,
} from "@mantine/core";
import { useQuery } from "@tanstack/react-query";
import { AddItemForm } from "./AddItemForm";
import { useTRPC } from "./api";
import { ItemRow } from "./ItemRow";

export function ItemList() {
  const trpc = useTRPC();
  const items = useQuery(trpc.listItems.queryOptions());

  return (
    <Stack>
      <AddItemForm />
      {items.isError && (
        <Alert color="red" title="Could not load your list">
          <Button
            variant="light"
            color="red"
            size="xs"
            loading={items.isFetching}
            onClick={() => void items.refetch()}
          >
            Retry
          </Button>
        </Alert>
      )}
      {items.isPending && (
        <Center>
          <Loader aria-label="Loading your list" />
        </Center>
      )}
      {items.data?.length === 0 && (
        <Text c="dimmed">
          Nothing on your list yet. Add the first item above.
        </Text>
      )}
      {items.data !== undefined && items.data.length > 0 && (
        <List
          type="ordered"
          listStyleType="none"
          spacing="xs"
          aria-label="Items"
        >
          {items.data.map((item) => (
            <ItemRow key={item.id} item={item} />
          ))}
        </List>
      )}
    </Stack>
  );
}
