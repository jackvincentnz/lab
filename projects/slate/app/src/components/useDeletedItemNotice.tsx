import { type RefObject, useEffect, useRef } from "react";
import { Button, Group, Text } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconArrowBackUp } from "@tabler/icons-react";
import type { Item } from "../api";

const AUTO_CLOSE_MS = 8000;

interface DeletedItemNoticeOptions {
  disabled: boolean;
  onUndo(item: Item): void;
  onDismiss(): void;
}

export function useDeletedItemNotice(
  item: Item | undefined,
  options: DeletedItemNoticeOptions,
) {
  const latest = useRef(options);
  const shownId = useRef<string>(undefined);

  useEffect(() => {
    latest.current = options;
  });

  useEffect(() => {
    if (item === undefined) {
      return;
    }
    let closedByPage = false;
    const id = notifications.show({
      message: message(item, latest),
      color: "gray",
      withBorder: true,
      autoClose: AUTO_CLOSE_MS,
      closeButtonProps: { "aria-label": "Dismiss" },
      onClose: () => {
        if (!closedByPage) {
          latest.current.onDismiss();
        }
      },
    });
    shownId.current = id;

    return () => {
      closedByPage = true;
      shownId.current = undefined;
      notifications.hide(id);
    };
  }, [item]);

  useEffect(() => {
    if (item !== undefined && shownId.current !== undefined) {
      notifications.update({
        id: shownId.current,
        message: message(item, latest),
      });
    }
  }, [item, options.disabled]);
}

function message(item: Item, latest: RefObject<DeletedItemNoticeOptions>) {
  return (
    <Group justify="space-between" wrap="nowrap">
      <Text size="sm">Deleted {item.name}</Text>
      <Button
        variant="light"
        size="xs"
        leftSection={<IconArrowBackUp size={14} />}
        disabled={latest.current.disabled}
        onClick={() => latest.current.onUndo(item)}
        autoFocus
      >
        Undo
      </Button>
    </Group>
  );
}
