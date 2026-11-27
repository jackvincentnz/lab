import { useLayoutEffect, useRef } from "react";
import { ActionIcon, Box, Button, Group, Stack } from "@mantine/core";
import { IconX } from "@tabler/icons-react";
import type { Item, ItemSource as Source, ItemStatus } from "../api";
import classes from "./ItemPanel.module.css";
import { ItemDescription } from "./ItemDescription";
import { ItemName } from "./ItemName";
import { ItemSource } from "./ItemSource";
import { StatusMenu } from "./StatusMenu";
import type { SaveDraft } from "./useItemDraft";

interface ItemPanelProps {
  item: Item;
  width: number;
  disabled: boolean;
  saving: boolean;
  onClose(): void;
  onRename: SaveDraft;
  onRenameCancel(): void;
  onDescribe: SaveDraft;
  onDescribeCancel(): void;
  onSourceChange: SaveDraft<Source | null>;
  onSourceChangeCancel(): void;
  onStatusChange(status: ItemStatus): void;
  onDelete(): void;
}

export function ItemPanel({
  item,
  width,
  disabled,
  saving,
  onClose,
  onRename,
  onRenameCancel,
  onDescribe,
  onDescribeCancel,
  onSourceChange,
  onSourceChangeCancel,
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
      pos={{ base: "static", md: "sticky" }}
      top="var(--app-shell-header-height, 0px)"
      h={{ md: "calc(100dvh - var(--app-shell-header-height, 0px))" }}
      display="flex"
      style={{
        "--item-panel-width": `${width}px`,
        flexShrink: 0,
        overflowY: "auto",
      }}
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
            disabled={disabled}
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
          disabled={disabled}
          saving={saving}
          onRename={onRename}
          onRenameCancel={onRenameCancel}
        />
        <ItemDescription
          key={`${item.id}-description`}
          item={item}
          disabled={disabled}
          saving={saving}
          onDescribe={onDescribe}
          onDescribeCancel={onDescribeCancel}
        />
        <ItemSource
          key={`${item.id}-source`}
          item={item}
          disabled={disabled}
          saving={saving}
          onSourceChange={onSourceChange}
          onSourceChangeCancel={onSourceChangeCancel}
        />
        <Group mt="auto">
          <Button
            variant="outline"
            color="red"
            aria-label={`Delete ${item.name}`}
            disabled={disabled}
            onClick={onDelete}
          >
            Delete
          </Button>
        </Group>
      </Stack>
    </Box>
  );
}
