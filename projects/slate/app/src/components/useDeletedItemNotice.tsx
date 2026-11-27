import { type RefObject, useEffect, useRef } from "react";
import { Button, Stack, Text } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconArrowBackUp } from "@tabler/icons-react";
import type { Item } from "../api";

const AUTO_CLOSE_MS = 8000;

export interface DeletedItem {
  item: Item;
  withKeyboard: boolean;
}

interface DeletedItemNoticeOptions {
  disabled: boolean;
  onUndo(item: Item): void;
  onDismiss(): void;
}

interface ShownNotice {
  id: string;
  deleted: DeletedItem;
}

export function useDeletedItemNotice(
  deleted: DeletedItem | undefined,
  options: DeletedItemNoticeOptions,
) {
  const latest = useRef(options);
  const shown = useRef<ShownNotice>(undefined);

  useEffect(() => {
    latest.current = options;
  });

  useEffect(() => {
    if (deleted === undefined) {
      return;
    }
    let closedByPage = false;
    const id = notifications.show({
      title: "Deleted",
      message: message(deleted, latest),
      color: "gray",
      withBorder: true,
      autoClose: AUTO_CLOSE_MS,
      closeButtonProps: { "aria-label": "Dismiss" },
      styles: { closeButton: { alignSelf: "flex-start" } },
      onClose: () => {
        if (!closedByPage) {
          latest.current.onDismiss();
        }
      },
    });
    shown.current = { id, deleted };

    return () => {
      closedByPage = true;
      shown.current = undefined;
      notifications.hide(id);
    };
  }, [deleted]);

  useEffect(() => {
    if (shown.current !== undefined) {
      notifications.update({
        id: shown.current.id,
        message: message(shown.current.deleted, latest),
      });
    }
  }, [options.disabled]);
}

function message(
  { item, withKeyboard }: DeletedItem,
  latest: RefObject<DeletedItemNoticeOptions>,
) {
  return (
    <Stack gap="xs" align="flex-start">
      <Text size="sm">{item.name}</Text>
      <Button
        variant="light"
        size="xs"
        leftSection={<IconArrowBackUp size={14} />}
        disabled={latest.current.disabled}
        onClick={() => latest.current.onUndo(item)}
        // Focus keeps the notification open, so it moves there only for a keyboard user, who could not reach it otherwise.
        autoFocus={withKeyboard}
      >
        Undo
      </Button>
    </Stack>
  );
}
