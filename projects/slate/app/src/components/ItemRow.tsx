import { useState, type FormEvent } from "react";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import {
  ActionIcon,
  Button,
  Group,
  List,
  NativeSelect,
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

interface ItemRowProps {
  item: Item;
  saving: boolean;
  onRename(name: string, onSaved: () => void): void;
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
  const [draft, setDraft] = useState<string>();
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
    if (draft !== undefined) {
      onRename(draft, () =>
        setDraft((current) => (current === draft ? undefined : current)),
      );
    }
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
              onClick={() => setDraft(item.name)}
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
            <Group wrap="nowrap">
              <TextInput
                aria-label="Name"
                value={draft}
                onChange={(event) => setDraft(event.currentTarget.value)}
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
                disabled={saving || draft.trim() === ""}
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
          </form>
        )}
      </Group>
    </List.Item>
  );
}
