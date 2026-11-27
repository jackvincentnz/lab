import { useEffect, useState } from "react";
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

type ItemEdit = Pick<UpdateItemInput, "name" | "description" | "source">;

const SLOW_SAVE_MS = 300;

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
  const disabled = useSlowSave(saving);
  const changedElsewhere = isChangedElsewhere(updateItem.error);
  const [deleted, setDeleted] = useState<Item>();
  const [openId, setOpenId] = useState<string>();
  const opened = items.data?.find((item) => item.id === openId);
  if (openId !== undefined && items.data !== undefined && !opened) {
    setOpenId(undefined);
  }

  function clearSaveError() {
    for (const change of changes) {
      if (!change.isPending) {
        change.reset();
      }
    }
  }

  // Each change carries the updatedAt of the list that reloads after the previous change, so changes go one at a time.
  function startChange() {
    if (saving) {
      return false;
    }
    clearSaveError();
    setDeleted(undefined);
    return true;
  }

  function changeStatus(
    item: Item,
    status: ItemStatus,
    onSettled?: () => void,
  ) {
    if (!startChange()) {
      return false;
    }
    updateItem.mutate(
      { id: item.id, status, updatedAt: item.updatedAt },
      { onSettled },
    );
    return true;
  }

  function saveEdit(
    editedFrom: Item,
    change: ItemEdit,
    { onSaved, onChanged }: SaveCallbacks,
  ) {
    if (!startChange()) {
      return;
    }
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
    <Flex direction={{ base: "column-reverse", md: "row" }} align="flex-start">
      <Stack flex={1} miw={0} w="100%" p={{ base: "md", md: "xl" }}>
        <AddItemForm
          onAdd={(name, onSaved) => {
            if (!startChange()) {
              return;
            }
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
            disabled={disabled}
            onUndo={() => {
              if (saving) {
                return;
              }
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
            disabled={disabled}
            openId={opened?.id}
            onMove={(move, onSettled) => {
              if (!startChange()) {
                return false;
              }
              moveItem.mutate(move, { onSettled });
              return true;
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
          disabled={disabled}
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
          onSourceChange={(editedFrom, source, callbacks) =>
            saveEdit(editedFrom, { source }, callbacks)
          }
          onSourceChangeCancel={clearSaveError}
          onStatusChange={(status) => changeStatus(opened, status)}
          onDelete={() => {
            if (!startChange()) {
              return;
            }
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

function useSlowSave(saving: boolean) {
  const [slow, setSlow] = useState(false);

  useEffect(() => {
    if (!saving) {
      return;
    }
    const timer = setTimeout(() => setSlow(true), SLOW_SAVE_MS);
    return () => {
      clearTimeout(timer);
      setSlow(false);
    };
  }, [saving]);

  return slow;
}
