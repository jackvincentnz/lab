import { useState } from "react";
import {
  Alert,
  Button,
  Center,
  Flex,
  Loader,
  Stack,
  Text,
} from "@mantine/core";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  isChangedElsewhere,
  type Item,
  type ItemStatus,
  type UpdateItemInput,
  useTRPC,
} from "../api";
import { AddItemForm } from "../components/AddItemForm";
import { DeletedItemNotice } from "../components/DeletedItemNotice";
import { ItemList } from "../components/ItemList";
import { ItemPanel } from "../components/ItemPanel";
import type { SaveCallbacks } from "../components/useItemDraft";

type ItemEdit = Pick<UpdateItemInput, "name" | "description">;

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
  const [openId, setOpenId] = useState<string>();
  const opened = items.data?.find((item) => item.id === openId);

  function clearSaveError() {
    for (const change of changes) {
      if (!change.isPending) {
        change.reset();
      }
    }
  }

  function startChange() {
    clearSaveError();
    setDeleted(undefined);
  }

  function changeStatus(item: Item, status: ItemStatus) {
    startChange();
    updateItem.mutate({ id: item.id, status, updatedAt: item.updatedAt });
  }

  function saveEdit(
    editedFrom: Item,
    change: ItemEdit,
    { onSaved, onChanged }: SaveCallbacks,
  ) {
    startChange();
    updateItem.mutate(
      { id: editedFrom.id, ...change, updatedAt: editedFrom.updatedAt },
      {
        onSuccess: onSaved,
        onError: (error) => {
          if (isChangedElsewhere(error)) {
            onChanged();
          }
        },
      },
    );
  }

  return (
    <Flex
      direction={{ base: "column-reverse", md: "row" }}
      align="flex-start"
      gap="lg"
    >
      <Stack flex={1} miw={0} w="100%">
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
            Check the item before you try again.
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
            openId={opened?.id}
            onMove={(move, onSettled) => {
              startChange();
              moveItem.mutate(move, { onSettled });
            }}
            onOpen={(item) => {
              clearSaveError();
              setOpenId(item.id);
            }}
            onStatusChange={changeStatus}
          />
        )}
      </Stack>
      {opened !== undefined && (
        <ItemPanel
          item={opened}
          saving={saving}
          onClose={() => {
            clearSaveError();
            setOpenId(undefined);
          }}
          onRename={(editedFrom, name, callbacks) =>
            saveEdit(editedFrom, { name }, callbacks)
          }
          onRenameCancel={clearSaveError}
          onDescribe={(editedFrom, description, callbacks) =>
            saveEdit(editedFrom, { description }, callbacks)
          }
          onDescribeCancel={clearSaveError}
          onStatusChange={(status) => changeStatus(opened, status)}
          onDelete={() => {
            startChange();
            deleteItem.mutate(
              { id: opened.id },
              {
                onSuccess: (item) => {
                  setOpenId((current) =>
                    current === item.id ? undefined : current,
                  );
                  setDeleted(item);
                },
              },
            );
          }}
        />
      )}
    </Flex>
  );
}
