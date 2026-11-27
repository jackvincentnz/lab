import { useLayoutEffect, useRef } from "react";
import { ActionIcon, Box, Button, Group, Stack } from "@mantine/core";
import { IconX } from "@tabler/icons-react";
import type { Item, ItemStatus } from "../api";
import classes from "./ItemPanel.module.css";
import { ItemDescription } from "./ItemDescription";
import { ItemName } from "./ItemName";
import { StatusMenu } from "./StatusMenu";
import type { SaveDraft } from "./useItemDraft";

interface ItemPanelProps {
  item: Item;
  saving: boolean;
  pending: boolean;
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
  pending,
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
    <Box
      ref={panel}
      component="aside"
      aria-label="Item details"
      tabIndex={-1}
      className={classes["panel"]}
      p="lg"
      w={{ base: "100%", md: 440 }}
      pos={{ base: "static", md: "sticky" }}
      top="var(--app-shell-header-height, 0px)"
      h={{ md: "calc(100dvh - var(--app-shell-header-height, 0px))" }}
      display="flex"
      style={{ flexShrink: 0, overflowY: "auto" }}
      onKeyDown={(event) => {
        if (event.key === "Escape" && !event.defaultPrevented) {
          onClose();
        }
      }}
    >
      <Stack flex={1}>
        <Group justify="space-between" wrap="nowrap">
          <StatusMenu
            status={item.status}
            saving={saving}
            onChange={onStatusChange}
          />
          <ActionIcon
            variant="default"
            size="input-xs"
            aria-label="Close details"
            onClick={onClose}
          >
            <IconX size={16} />
          </ActionIcon>
        </Group>
        <ItemName
          key={item.id}
          item={item}
          saving={saving}
          pending={pending}
          onRename={onRename}
          onRenameCancel={onRenameCancel}
        />
        <ItemDescription
          key={`${item.id}-description`}
          item={item}
          saving={saving}
          pending={pending}
          onDescribe={onDescribe}
          onDescribeCancel={onDescribeCancel}
        />
        <Group mt="auto">
          <Button
            variant="outline"
            color="red"
            aria-label={`Delete ${item.name}`}
            disabled={saving}
            onClick={onDelete}
          >
            Delete
          </Button>
        </Group>
      </Stack>
    </Box>
  );
}
