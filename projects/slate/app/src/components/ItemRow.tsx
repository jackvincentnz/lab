import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { ActionIcon, Group, List, NativeSelect } from "@mantine/core";
import { IconGripVertical, IconTrash } from "@tabler/icons-react";
import type { Item, ItemStatus } from "../api";
import { ItemName, type RenameCallbacks } from "./ItemName";

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
  const {
    attributes,
    listeners,
    setNodeRef,
    setActivatorNodeRef,
    transform,
    transition,
  } = useSortable({ id: item.id, disabled: saving });

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
        <ItemName
          item={item}
          saving={saving}
          onRename={onRename}
          onRenameCancel={onRenameCancel}
        >
          <ActionIcon
            variant="subtle"
            color="gray"
            aria-label={`Delete ${item.name}`}
            disabled={saving}
            onClick={onDelete}
          >
            <IconTrash size={16} />
          </ActionIcon>
        </ItemName>
      </Group>
    </List.Item>
  );
}
