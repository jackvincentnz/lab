import { useState, type FormEvent, type ReactNode } from "react";
import { Button, Group, Stack, Text, TextInput } from "@mantine/core";
import type { Item } from "../api";

export interface RenameCallbacks {
  onSaved(saved: Item): void;
  onChanged(): void;
}

interface Draft {
  name: string;
  editedFrom: Item;
  refusedFrom?: Item;
}

interface ItemNameProps {
  item: Item;
  saving: boolean;
  onRename(editedFrom: Item, name: string, callbacks: RenameCallbacks): void;
  onRenameCancel(): void;
  children?: ReactNode;
}

export function ItemName({
  item,
  saving,
  onRename,
  onRenameCancel,
  children,
}: ItemNameProps) {
  const [draft, setDraft] = useState<Draft>();

  function submitRename(event: FormEvent) {
    event.preventDefault();
    if (draft === undefined) {
      return;
    }
    // A rename overwrites only the name, so only a name changed elsewhere makes its draft stale.
    const nameChangedElsewhere = item.name !== draft.editedFrom.name;
    const editedFrom =
      draft.refusedFrom !== undefined || !nameChangedElsewhere
        ? item
        : draft.editedFrom;
    onRename(editedFrom, draft.name, {
      onSaved: (saved) =>
        setDraft((current) =>
          current === undefined || current.name === draft.name
            ? undefined
            : { name: current.name, editedFrom: saved },
        ),
      onChanged: () =>
        setDraft(
          (current) => current && { ...current, refusedFrom: editedFrom },
        ),
    });
  }

  function cancelRename() {
    onRenameCancel();
    setDraft(undefined);
  }

  if (draft === undefined) {
    return (
      <>
        <Text
          flex={1}
          td={item.status === "done" ? "line-through" : undefined}
          c={item.status === "done" ? "dimmed" : undefined}
        >
          {item.name}
        </Text>
        <Button
          variant="subtle"
          size="xs"
          aria-label={`Rename ${item.name}`}
          onClick={() => setDraft({ name: item.name, editedFrom: item })}
        >
          Rename
        </Button>
        {children}
      </>
    );
  }

  return (
    <form onSubmit={submitRename} style={{ flex: 1 }}>
      <Stack gap={4}>
        <Group wrap="nowrap">
          <TextInput
            aria-label="Name"
            value={draft.name}
            onChange={(event) => {
              const name = event.currentTarget.value;
              setDraft((current) => current && { ...current, name });
            }}
            onKeyDown={(event) => {
              if (event.key === "Escape" && !saving) {
                cancelRename();
              }
            }}
            autoFocus
            flex={1}
          />
          <Button
            type="submit"
            size="xs"
            disabled={saving || draft.name.trim() === ""}
          >
            Save
          </Button>
          <Button
            variant="subtle"
            size="xs"
            disabled={saving}
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
