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
import { itemLink } from "../itemLink";
import { ItemList } from "../components/ItemList";
import { ItemPanel } from "../components/ItemPanel";
import { ListHeading } from "../components/ListHeading";
import { PanelResizeHandle } from "../components/PanelResizeHandle";
import { ViewList } from "../components/ViewList";
import {
  type DeletedItem,
  useDeletedItemNotice,
} from "../components/useDeletedItemNotice";
import type { SaveCallbacks } from "../components/useItemDraft";
import { useResizablePanel } from "../components/useResizablePanel";
import { searchView, type ViewName, views } from "../views";

type ItemEdit = Pick<
  UpdateItemInput,
  "name" | "description" | "addSources" | "removeSources" | "replaceSource"
>;

const SLOW_SAVE_MS = 300;

interface ItemsPageProps {
  view: ViewName;
  search?: string;
  initialOpenId?: string | undefined;
}

export function ItemsPage({ view, search, initialOpenId }: ItemsPageProps) {
  const searching = search !== undefined;
  const { label, query, empty } = searching ? searchView(search) : views[view];
  const today = !searching && view === "today";
  const done = !searching && view === "done";
  const headed = !today && !done;
  const trpc = useTRPC();
  // Keeps the last results while the next search loads, instead of a loader on each word.
  const items = useQuery({
    ...trpc.listItems.queryOptions(query),
    placeholderData: (previous) => (previous?.length ? previous : undefined),
  });
  const addItem = useMutation(trpc.addItem.mutationOptions());
  const updateItem = useMutation(trpc.updateItem.mutationOptions());
  const moveItem = useMutation(trpc.moveItem.mutationOptions());
  const deleteItem = useMutation(trpc.deleteItem.mutationOptions());
  const restoreItem = useMutation(trpc.restoreItem.mutationOptions());
  const changes = [addItem, updateItem, moveItem, deleteItem, restoreItem];
  const saving = changes.some((change) => change.isPending);
  const disabled = useSlowSave(saving);
  const panel = useResizablePanel();
  const changedElsewhere = isChangedElsewhere(updateItem.error);
  const [deleted, setDeleted] = useState<DeletedItem>();
  const [openId, setOpenId] = useState(initialOpenId);
  const opened = items.data?.find((item) => item.id === openId);
  if (openId !== undefined && items.data !== undefined && !opened) {
    setOpenId(undefined);
  }
  useDeletedItemNotice(deleted, {
    disabled,
    restoring: restoreItem.isPending,
    onUndo: (item) => {
      if (saving) {
        return;
      }
      clearSaveError();
      restoreItem.mutate(
        { id: item.id },
        { onSuccess: () => setDeleted(undefined) },
      );
    },
    onDismiss: () => setDeleted(undefined),
  });

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
    <Flex
      ref={panel.containerRef}
      direction={{ base: "column-reverse", md: "row" }}
      align="flex-start"
    >
      <Stack flex={1} miw={0} w="100%" p={{ base: "md", md: "xl" }}>
        {today && (
          <AddItemForm
            onAdd={(name, onSaved) => {
              if (!startChange()) {
                return;
              }
              addItem.mutate({ name }, { onSuccess: onSaved });
            }}
          />
        )}
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
        {(headed || items.isPending || items.data !== undefined) && (
          <Stack gap="xs">
            {headed && (
              <ListHeading>
                {label}
                {searching &&
                  items.data !== undefined &&
                  ` · ${items.data.length}`}
              </ListHeading>
            )}
            {items.isPending && (
              <Center>
                <Loader aria-label="Loading your list" />
              </Center>
            )}
            {items.data?.length === 0 && <Text c="dimmed">{empty}</Text>}
            {items.data !== undefined && items.data.length > 0 && !today && (
              <ViewList
                label={label}
                items={items.data}
                groupByDayDone={done}
                showStatus={searching}
                disabled={disabled}
                openId={opened?.id}
                onOpen={(item) => {
                  clearSaveError();
                  setOpenId(item.id);
                }}
                onStatusChange={changeStatus}
                {...(view === "deleted" && {
                  onRestore: (item: Item) => {
                    if (!startChange()) {
                      return;
                    }
                    restoreItem.mutate({ id: item.id });
                  },
                })}
              />
            )}
            {items.data !== undefined && items.data.length > 0 && today && (
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
        )}
      </Stack>
      {opened !== undefined && (
        <PanelResizeHandle handleProps={panel.handleProps} />
      )}
      {opened !== undefined && (
        <ItemPanel
          item={opened}
          link={itemLink(opened)}
          width={panel.width}
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
          onSourcesChange={(editedFrom, change, callbacks) =>
            saveEdit(editedFrom, change, callbacks)
          }
          onSourcesChangeCancel={clearSaveError}
          onStatusChange={(status) => changeStatus(opened, status)}
          onDelete={(withKeyboard) => {
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
                  setDeleted({ item, withKeyboard });
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
