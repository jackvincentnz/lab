import { useState } from "react";
import {
  closestCenter,
  DndContext,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { List } from "@mantine/core";
import type { Item, ItemStatus, MoveItemInput } from "../api";
import { type RenameCallbacks } from "./ItemName";
import { ItemRow } from "./ItemRow";

interface ItemListProps {
  items: Item[];
  saving: boolean;
  onMove(move: MoveItemInput, onSettled: () => void): void;
  onRename(editedFrom: Item, name: string, callbacks: RenameCallbacks): void;
  onRenameCancel(): void;
  onStatusChange(item: Item, status: ItemStatus): void;
  onDelete(item: Item): void;
}

export function ItemList({
  items,
  saving,
  onMove,
  onRename,
  onRenameCancel,
  onStatusChange,
  onDelete,
}: ItemListProps) {
  const [droppedOrder, setDroppedOrder] = useState<Item[]>();
  const shownItems = droppedOrder ?? items;
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  );

  function drop({ active, over }: DragEndEvent) {
    if (over === null || active.id === over.id) {
      return;
    }
    const from = items.findIndex((item) => item.id === active.id);
    const to = items.findIndex((item) => item.id === over.id);
    // dnd-kit clears the drag offset on drop, so without the new order in the same render the row jumps back first.
    setDroppedOrder(arrayMove(items, from, to));
    onMove(
      {
        id: String(active.id),
        neighbourId: String(over.id),
        position: from < to ? "after" : "before",
      },
      () => setDroppedOrder(undefined),
    );
  }

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragEnd={drop}
    >
      <SortableContext
        items={shownItems}
        strategy={verticalListSortingStrategy}
      >
        <List
          type="ordered"
          listStyleType="none"
          spacing="xs"
          aria-label="Items"
        >
          {shownItems.map((item) => (
            <ItemRow
              key={item.id}
              item={item}
              saving={saving}
              onRename={onRename}
              onRenameCancel={onRenameCancel}
              onStatusChange={(status) => onStatusChange(item, status)}
              onDelete={() => onDelete(item)}
            />
          ))}
        </List>
      </SortableContext>
    </DndContext>
  );
}
