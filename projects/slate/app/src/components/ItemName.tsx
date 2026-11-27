import type { FormEvent } from "react";
import {
  Button,
  Group,
  Stack,
  Text,
  TextInput,
  Title,
  UnstyledButton,
} from "@mantine/core";
import type { Item } from "../api";
import { useItemDraft, type SaveDraft } from "./useItemDraft";

interface ItemNameProps {
  item: Item;
  disabled: boolean;
  saving: boolean;
  onRename: SaveDraft;
  onRenameCancel(): void;
}

export function ItemName({
  item,
  disabled,
  saving,
  onRename,
  onRenameCancel,
}: ItemNameProps) {
  const { draft, start, change, submit, cancel } = useItemDraft(
    item,
    nameOf,
    onRename,
  );

  function submitRename(event: FormEvent) {
    event.preventDefault();
    submit();
  }

  function cancelRename() {
    if (!saving) {
      onRenameCancel();
      cancel();
    }
  }

  if (draft === undefined) {
    return (
      <Title order={2} size="h3">
        <UnstyledButton
          aria-label={`Rename ${item.name}`}
          onClick={start}
          style={{ font: "inherit" }}
        >
          {item.name}
        </UnstyledButton>
      </Title>
    );
  }

  return (
    <form onSubmit={submitRename}>
      <Stack gap={4}>
        <Group wrap="nowrap">
          <TextInput
            aria-label="Name"
            value={draft.value}
            onChange={(event) => change(event.currentTarget.value)}
            onKeyDown={(event) => {
              if (event.key === "Escape") {
                event.preventDefault();
                cancelRename();
              }
            }}
            autoFocus
            flex={1}
          />
          <Button
            type="submit"
            size="xs"
            disabled={disabled || draft.value.trim() === ""}
          >
            Save
          </Button>
          <Button
            variant="subtle"
            size="xs"
            disabled={disabled}
            onClick={cancelRename}
          >
            Cancel
          </Button>
        </Group>
        {draft.refusedFrom !== undefined && (
          <Text size="xs" c="dimmed">
            {item.updatedAt === draft.refusedFrom.updatedAt
              ? "Changed elsewhere."
              : `Changed elsewhere. Current name: ${item.name}`}
          </Text>
        )}
      </Stack>
    </form>
  );
}

function nameOf(item: Item) {
  return item.name;
}
