import { useLayoutEffect, useRef } from "react";
import { ActionIcon, Box, Button, Group, Stack } from "@mantine/core";
import { IconX } from "@tabler/icons-react";
import type { Item, ItemChange, ItemStatus } from "../api";
import classes from "./ItemPanel.module.css";
import { ItemChanges } from "./ItemChanges";
import { ItemDescription } from "./ItemDescription";
import { RelatedItems } from "./RelatedItems";
import { ItemName } from "./ItemName";
import { ItemSources, type SourcesChange } from "./ItemSources";
import { StatusMenu } from "./StatusMenu";
import type { SaveDraft } from "./useItemDraft";

interface ItemPanelProps {
  item: Item;
  changes?: ItemChange[];
  related?: Item[];
  relatedCandidates?: Item[];
  openableIds?: string[];
  onOpenRelated(id: string): void;
  onRelate(relatedId: string): void;
  onUnrelate(relatedId: string): void;
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
  changes = [],
  related = [],
  relatedCandidates = [],
  openableIds = [],
  onOpenRelated,
  onRelate,
  onUnrelate,
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
        <ItemSources
          key={`${item.id}-sources`}
          item={item}
          disabled={disabled}
          saving={saving}
          onSourcesChange={onSourcesChange}
          onSourcesChangeCancel={onSourcesChangeCancel}
        />
        <RelatedItems
          related={related}
          candidates={relatedCandidates}
          openableIds={openableIds}
          disabled={disabled}
          onOpen={onOpenRelated}
          onRelate={onRelate}
          onUnrelate={onUnrelate}
        />
        <ItemChanges changes={changes} />
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
