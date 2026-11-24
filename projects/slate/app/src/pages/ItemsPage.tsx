import { Alert, Button, Center, Loader, Stack, Text } from "@mantine/core";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useTRPC } from "../api";
import { AddItemForm } from "../components/AddItemForm";
import { ItemList } from "../components/ItemList";

export function ItemsPage() {
  const trpc = useTRPC();
  const items = useQuery(trpc.listItems.queryOptions());
  const addItem = useMutation(trpc.addItem.mutationOptions());
  const updateItem = useMutation(trpc.updateItem.mutationOptions());
  const moveItem = useMutation(trpc.moveItem.mutationOptions());
  const saving =
    addItem.isPending || updateItem.isPending || moveItem.isPending;

  function clearSaveError() {
    addItem.reset();
    updateItem.reset();
    moveItem.reset();
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
      {(addItem.isError || updateItem.isError || moveItem.isError) && (
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
        <ItemList
          items={items.data}
          saving={saving}
          onMove={(move, onSettled) => {
            clearSaveError();
            moveItem.mutate(move, { onSettled });
          }}
          onRename={(item, name, onSaved) => {
            clearSaveError();
            updateItem.mutate({ id: item.id, name }, { onSuccess: onSaved });
          }}
          onRenameCancel={clearSaveError}
          onStatusChange={(item, status) => {
            clearSaveError();
            updateItem.mutate({ id: item.id, status });
          }}
        />
      )}
    </Stack>
  );
}
