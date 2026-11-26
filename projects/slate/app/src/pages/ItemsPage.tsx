import { useState } from "react";
import { Alert, Button, Center, Loader, Stack, Text } from "@mantine/core";
import { useMutation, useQuery } from "@tanstack/react-query";
import { type Item, useTRPC } from "../api";
import { AddItemForm } from "../components/AddItemForm";
import { DeletedItemNotice } from "../components/DeletedItemNotice";
import { ItemList } from "../components/ItemList";

export function ItemsPage() {
  const trpc = useTRPC();
  const items = useQuery({
    ...trpc.listItems.queryOptions(),
    refetchInterval: () => untilMidnight(new Date()),
  });
  const addItem = useMutation(trpc.addItem.mutationOptions());
  const updateItem = useMutation(trpc.updateItem.mutationOptions());
  const moveItem = useMutation(trpc.moveItem.mutationOptions());
  const deleteItem = useMutation(trpc.deleteItem.mutationOptions());
  const restoreItem = useMutation(trpc.restoreItem.mutationOptions());
  const changes = [addItem, updateItem, moveItem, deleteItem, restoreItem];
  const saving = changes.some((change) => change.isPending);
  const [deleted, setDeleted] = useState<Item>();

  function clearSaveError() {
    for (const change of changes) {
      change.reset();
    }
  }

  function startChange() {
    clearSaveError();
    setDeleted(undefined);
  }

  return (
    <Stack>
      <AddItemForm
        saving={saving}
        onAdd={(name, onSaved) => {
          startChange();
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
      {changes.some((change) => change.isError) && (
        <Alert color="red" title="Could not save your change" />
      )}
      {deleted !== undefined && (
        <DeletedItemNotice
          item={deleted}
          saving={saving}
          onUndo={() => {
            clearSaveError();
            restoreItem.mutate(
              { id: deleted.id },
              { onSuccess: () => setDeleted(undefined) },
            );
          }}
          onDismiss={() => setDeleted(undefined)}
        />
      )}
      {items.isPending && (
        <Center>
          <Loader aria-label="Loading your list" />
        </Center>
      )}
      {items.data?.length === 0 && (
        <Text c="dimmed">
          Nothing on your slate yet. Add the first item above.
        </Text>
      )}
      {items.data !== undefined && items.data.length > 0 && (
        <ItemList
          items={items.data}
          saving={saving}
          onMove={(move, onSettled) => {
            startChange();
            moveItem.mutate(move, { onSettled });
          }}
          onRename={(item, name, onSaved) => {
            startChange();
            updateItem.mutate({ id: item.id, name }, { onSuccess: onSaved });
          }}
          onRenameCancel={clearSaveError}
          onStatusChange={(item, status) => {
            startChange();
            updateItem.mutate({ id: item.id, status });
          }}
          onDelete={(item) => {
            startChange();
            deleteItem.mutate({ id: item.id }, { onSuccess: setDeleted });
          }}
        />
      )}
    </Stack>
  );
}

/** The service hides items marked done before today, so a list left open reloads when the day changes. */
function untilMidnight(now: Date): number {
  const midnight = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate() + 1,
  );
  return midnight.getTime() - now.getTime();
}
