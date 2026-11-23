import { useState, type FormEvent } from "react";
import {
  Button,
  Group,
  List,
  NativeSelect,
  Text,
  TextInput,
} from "@mantine/core";
import type { Item, ItemStatus } from "./api";

const statusOptions = [
  { value: "todo", label: "To do" },
  { value: "doing", label: "Doing" },
  { value: "done", label: "Done" },
];

interface ItemRowProps {
  item: Item;
  saving: boolean;
  onRename(name: string, onSaved: () => void): void;
  onRenameCancel(): void;
  onStatusChange(status: ItemStatus): void;
}

export function ItemRow({
  item,
  saving,
  onRename,
  onRenameCancel,
  onStatusChange,
}: ItemRowProps) {
  const [draft, setDraft] = useState<string>();

  function submitRename(event: FormEvent) {
    event.preventDefault();
    if (draft !== undefined) {
      onRename(draft, () => setDraft(undefined));
    }
  }

  function cancelRename() {
    onRenameCancel();
    setDraft(undefined);
  }

  return (
    <List.Item>
      <Group wrap="nowrap">
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
          </>
        ) : (
          <form onSubmit={submitRename} style={{ flex: 1 }}>
            <Group wrap="nowrap">
              <TextInput
                aria-label="Name"
                value={draft}
                onChange={(event) => setDraft(event.currentTarget.value)}
                onKeyDown={(event) => {
                  if (event.key === "Escape") {
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
              <Button variant="subtle" size="xs" onClick={cancelRename}>
                Cancel
              </Button>
            </Group>
          </form>
        )}
      </Group>
    </List.Item>
  );
}
