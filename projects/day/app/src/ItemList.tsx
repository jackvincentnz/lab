import {
  Alert,
  Button,
  Center,
  List,
  Loader,
  Stack,
  Text,
} from "@mantine/core";
import { useMutation, useQuery } from "@tanstack/react-query";
import { AddItemForm } from "./AddItemForm";
import { useTRPC } from "./api";
import { ItemRow } from "./ItemRow";

export function ItemList() {
  const trpc = useTRPC();
  const items = useQuery(trpc.listItems.queryOptions());
  const addItem = useMutation(trpc.addItem.mutationOptions());
  const updateItem = useMutation(trpc.updateItem.mutationOptions());
  const saving = addItem.isPending || updateItem.isPending;

  function clearSaveError() {
    addItem.reset();
    updateItem.reset();
  }

  return (
    <Stack>
      <AddItemForm
        saving={saving}
        onAdd={(name, onSaved) => {
          clearSaveError();
          addItem.mutate({ name }, { onSuccess: onSaved });
        }}
      />
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
      {(addItem.isError || updateItem.isError) && (
        <Alert color="red" title="Could not save your change" />
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
            <ItemRow
              key={item.id}
              item={item}
              saving={saving}
              onRename={(name, onSaved) => {
                clearSaveError();
                updateItem.mutate(
                  { id: item.id, name },
                  { onSuccess: onSaved },
                );
              }}
              onRenameCancel={clearSaveError}
              onStatusChange={(status) => {
                clearSaveError();
                updateItem.mutate({ id: item.id, status });
              }}
            />
          ))}
        </List>
      )}
    </Stack>
  );
}
