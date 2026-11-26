import { useLayoutEffect, useRef } from "react";
import { Button, CloseButton, Group, Paper, Stack } from "@mantine/core";
import { IconTrash } from "@tabler/icons-react";
import type { Item, ItemStatus } from "../api";
import { ItemDescription } from "./ItemDescription";
import { ItemName } from "./ItemName";
import { StatusMenu } from "./StatusMenu";
import type { SaveDraft } from "./useItemDraft";

interface ItemPanelProps {
  item: Item;
  saving: boolean;
  onClose(): void;
  onRename: SaveDraft;
  onRenameCancel(): void;
  onDescribe: SaveDraft;
  onDescribeCancel(): void;
  onStatusChange(status: ItemStatus): void;
  onDelete(): void;
}

export function ItemPanel({
  item,
  saving,
  onClose,
  onRename,
  onRenameCancel,
  onDescribe,
  onDescribeCancel,
  onStatusChange,
  onDelete,
}: ItemPanelProps) {
  const panel = useRef<HTMLElement>(null);
  const opener = useRef<Element | null>(null);

  useLayoutEffect(() => {
    opener.current = document.activeElement;
    panel.current?.focus();
  }, [item.id]);

  useLayoutEffect(
    () => () => {
      if (opener.current instanceof HTMLElement) {
        opener.current.focus();
      }
    },
    [],
  );

  return (
    <Paper
      ref={panel}
      component="aside"
      aria-label="Item details"
      tabIndex={-1}
      withBorder
      p="md"
      w={{ base: "100%", md: 420 }}
      pos={{ base: "static", md: "sticky" }}
      top="calc(var(--app-shell-header-height, 0px) + var(--mantine-spacing-md))"
      mah={{
        md: "calc(100dvh - var(--app-shell-header-height, 0px) - 2 * var(--mantine-spacing-md))",
      }}
      style={{ flexShrink: 0, overflowY: "auto" }}
      onKeyDown={(event) => {
        if (event.key === "Escape" && !event.defaultPrevented) {
          onClose();
        }
      }}
    >
      <Stack>
        <Group justify="space-between" wrap="nowrap">
          <StatusMenu
            status={item.status}
            saving={saving}
            onChange={onStatusChange}
          />
          <CloseButton aria-label="Close details" onClick={onClose} />
        </Group>
        <ItemName
          key={item.id}
          item={item}
          saving={saving}
          onRename={onRename}
          onRenameCancel={onRenameCancel}
        />
        <ItemDescription
          key={`${item.id}-description`}
          item={item}
          saving={saving}
          onDescribe={onDescribe}
          onDescribeCancel={onDescribeCancel}
        />
        <Group>
          <Button
            variant="light"
            color="red"
            size="xs"
            leftSection={<IconTrash size={14} />}
            aria-label={`Delete ${item.name}`}
            disabled={saving}
            onClick={onDelete}
          >
            Delete
          </Button>
        </Group>
      </Stack>
    </Paper>
  );
}
