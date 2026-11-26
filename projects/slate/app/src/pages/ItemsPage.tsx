import { useState } from "react";
import { Alert, Button, Center, Loader, Stack, Text } from "@mantine/core";
import { useMutation, useQuery } from "@tanstack/react-query";
import { isChangedElsewhere, type Item, useTRPC } from "../api";
import { AddItemForm } from "../components/AddItemForm";
import { DeletedItemNotice } from "../components/DeletedItemNotice";
import { ItemList } from "../components/ItemList";

export function ItemsPage() {
  const trpc = useTRPC();
  const items = useQuery(trpc.listItems.queryOptions());
  const addItem = useMutation(trpc.addItem.mutationOptions());
  const updateItem = useMutation(trpc.updateItem.mutationOptions());
  const moveItem = useMutation(trpc.moveItem.mutationOptions());
  const deleteItem = useMutation(trpc.deleteItem.mutationOptions());
  const restoreItem = useMutation(trpc.restoreItem.mutationOptions());
  const changes = [addItem, updateItem, moveItem, deleteItem, restoreItem];
  const saving = changes.some((change) => change.isPending);
  const changedElsewhere = isChangedElsewhere(updateItem.error);
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
      {changedElsewhere ? (
        <Alert color="yellow" title="Not saved: the item changed elsewhere">
          The list now shows it as it is. Make your change again if you still
          want it.
        </Alert>
      ) : (
        changes.some((change) => change.isError) && (
          <Alert color="red" title="Could not save your change" />
        )
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
          onRename={(editedFrom, name, { onSaved, onChanged }) => {
            startChange();
            updateItem.mutate(
              { id: editedFrom.id, name, updatedAt: editedFrom.updatedAt },
              {
                onSuccess: onSaved,
                onError: (error) => {
                  if (isChangedElsewhere(error)) {
                    onChanged();
                  }
                },
              },
            );
          }}
          onRenameCancel={clearSaveError}
          onStatusChange={(item, status) => {
            startChange();
            updateItem.mutate({
              id: item.id,
              status,
              updatedAt: item.updatedAt,
            });
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
