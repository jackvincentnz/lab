import { useLayoutEffect, useRef } from "react";
import { ActionIcon, Box, Button, Group, Stack } from "@mantine/core";
import { useClipboard } from "@mantine/hooks";
import { IconCheck, IconLink, IconX } from "@tabler/icons-react";
import type { Item, ItemStatus } from "../api";
import classes from "./ItemPanel.module.css";
import { ItemDescription } from "./ItemDescription";
import { ItemName } from "./ItemName";
import { ItemSources, type SourcesChange } from "./ItemSources";
import { StatusMenu } from "./StatusMenu";
import type { SaveDraft } from "./useItemDraft";

interface ItemPanelProps {
  item: Item;
  link: string;
  width: number;
  disabled: boolean;
  saving: boolean;
  onClose(): void;
  onRename: SaveDraft;
  onRenameCancel(): void;
  onDescribe: SaveDraft;
  onDescribeCancel(): void;
  onSourcesChange: SaveDraft<SourcesChange>;
  onSourcesChangeCancel(): void;
  onStatusChange(status: ItemStatus): void;
  onDelete(withKeyboard: boolean): void;
}

export function ItemPanel({
  item,
  link,
  width,
  disabled,
  saving,
  onClose,
  onRename,
  onRenameCancel,
  onDescribe,
  onDescribeCancel,
  onSourcesChange,
  onSourcesChangeCancel,
  onStatusChange,
  onDelete,
}: ItemPanelProps) {
  const panel = useRef<HTMLElement>(null);
  const opener = useRef<Element | null>(null);
  const clipboard = useClipboard();

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
          <Group gap="xs" wrap="nowrap">
            <ActionIcon
              variant="default"
              size="input-xs"
              aria-label={clipboard.copied ? "Link copied" : "Copy link"}
              onClick={() => clipboard.copy(link)}
            >
              {clipboard.copied ? (
                <IconCheck size={16} />
              ) : (
                <IconLink size={16} />
              )}
            </ActionIcon>
            <ActionIcon
              variant="default"
              size="input-xs"
              aria-label="Close details"
              onClick={onClose}
            >
              <IconX size={16} />
            </ActionIcon>
          </Group>
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
        <ItemSources
          key={`${item.id}-sources`}
          item={item}
          disabled={disabled}
          saving={saving}
          onSourcesChange={onSourcesChange}
          onSourcesChangeCancel={onSourcesChangeCancel}
        />
        <Group mt="auto">
          <Button
            variant="outline"
            color="red"
            aria-label={`Delete ${item.name}`}
            disabled={disabled}
            // A click from Enter or Space has no click count.
            onClick={(event) => onDelete(event.detail === 0)}
          >
            Delete
          </Button>
        </Group>
      </Stack>
    </Box>
  );
}
