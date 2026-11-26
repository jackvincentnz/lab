import { useState, type FormEvent } from "react";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import {
  ActionIcon,
  Button,
  Group,
  List,
  NativeSelect,
  Stack,
  Text,
  TextInput,
} from "@mantine/core";
import { IconGripVertical, IconTrash } from "@tabler/icons-react";
import type { Item, ItemStatus } from "../api";

const statusLabels: Record<ItemStatus, string> = {
  todo: "To do",
  doing: "Doing",
  done: "Done",
};

const statusOptions = Object.entries(statusLabels).map(([value, label]) => ({
  value,
  label,
}));

export interface RenameCallbacks {
  onSaved(saved: Item): void;
  onChanged(): void;
}

interface Draft {
  name: string;
  editedFrom: Item;
  changed: boolean;
}

interface ItemRowProps {
  item: Item;
  saving: boolean;
  onRename(editedFrom: Item, name: string, callbacks: RenameCallbacks): void;
  onRenameCancel(): void;
  onStatusChange(status: ItemStatus): void;
  onDelete(): void;
}

export function ItemRow({
  item,
  saving,
  onRename,
  onRenameCancel,
  onStatusChange,
  onDelete,
}: ItemRowProps) {
  const [draft, setDraft] = useState<Draft>();
  const {
    attributes,
    listeners,
    setNodeRef,
    setActivatorNodeRef,
    transform,
    transition,
  } = useSortable({ id: item.id, disabled: saving });

  function submitRename(event: FormEvent) {
    event.preventDefault();
    if (draft === undefined) {
      return;
    }
    // A rename overwrites only the name, so only a name changed elsewhere makes its draft stale.
    const nameChangedElsewhere = item.name !== draft.editedFrom.name;
    const editedFrom =
      draft.changed || !nameChangedElsewhere ? item : draft.editedFrom;
    onRename(editedFrom, draft.name, {
      onSaved: (saved) =>
        setDraft((current) =>
          current === undefined || current.name === draft.name
            ? undefined
            : { ...current, editedFrom: saved, changed: false },
        ),
      onChanged: () =>
        setDraft((current) => current && { ...current, changed: true }),
    });
  }

  function cancelRename() {
    onRenameCancel();
    setDraft(undefined);
  }

  return (
    <List.Item
      ref={setNodeRef}
      style={{ transform: CSS.Translate.toString(transform), transition }}
    >
      <Group wrap="nowrap">
        <ActionIcon
          ref={setActivatorNodeRef}
          variant="subtle"
          color="gray"
          aria-label={`Move ${item.name}`}
          data-disabled={saving}
          style={{ cursor: "grab" }}
          {...attributes}
          {...listeners}
        >
          <IconGripVertical size={16} />
        </ActionIcon>
        <NativeSelect
          aria-label={`Status of ${item.name}`}
          data={statusOptions}
          value={item.status}
          disabled={saving}
          onChange={(event) =>
            onStatusChange(event.currentTarget.value as ItemStatus)
          }
        />
        {draft === undefined ? (
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
              onClick={() =>
                setDraft({ name: item.name, editedFrom: item, changed: false })
              }
            >
              Rename
            </Button>
            <ActionIcon
              variant="subtle"
              color="gray"
              aria-label={`Delete ${item.name}`}
              disabled={saving}
              onClick={onDelete}
            >
              <IconTrash size={16} />
            </ActionIcon>
          </>
        ) : (
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
              {draft.changed && (
                <Text size="xs" c="dimmed">
                  Changed elsewhere. Current name: {item.name}
                </Text>
              )}
            </Stack>
          </form>
        )}
      </Group>
    </List.Item>
  );
}
