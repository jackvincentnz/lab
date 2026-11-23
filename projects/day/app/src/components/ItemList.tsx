import { List } from "@mantine/core";
import type { Item, ItemStatus } from "../api";
import { ItemRow } from "./ItemRow";

interface ItemListProps {
  items: Item[];
  saving: boolean;
  onRename(item: Item, name: string, onSaved: () => void): void;
  onRenameCancel(): void;
  onStatusChange(item: Item, status: ItemStatus): void;
}

export function ItemList({
  items,
  saving,
  onRename,
  onRenameCancel,
  onStatusChange,
}: ItemListProps) {
  return (
    <List type="ordered" listStyleType="none" spacing="xs" aria-label="Items">
      {items.map((item) => (
        <ItemRow
          key={item.id}
          item={item}
          saving={saving}
          onRename={(name, onSaved) => onRename(item, name, onSaved)}
          onRenameCancel={onRenameCancel}
          onStatusChange={(status) => onStatusChange(item, status)}
        />
      ))}
    </List>
  );
}
