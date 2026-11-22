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
  disabled: boolean;
  onRename(name: string): Promise<boolean>;
  onStatusChange(status: ItemStatus): Promise<boolean>;
}

export function ItemRow({
  item,
  disabled,
  onRename,
  onStatusChange,
}: ItemRowProps) {
  const [draft, setDraft] = useState<string>();

  async function rename(event: FormEvent) {
    event.preventDefault();
    if (draft !== undefined && (await onRename(draft))) {
      setDraft(undefined);
    }
  }

  return (
    <List.Item>
      <Group wrap="nowrap">
        <NativeSelect
          aria-label={`Status of ${item.name}`}
          data={statusOptions}
          value={item.status}
          disabled={disabled}
          onChange={(event) =>
            void onStatusChange(event.currentTarget.value as ItemStatus)
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
          <form onSubmit={rename} style={{ flex: 1 }}>
            <Group wrap="nowrap">
              <TextInput
                aria-label="Name"
                value={draft}
                onChange={(event) => setDraft(event.currentTarget.value)}
                onKeyDown={(event) => {
                  if (event.key === "Escape") {
                    setDraft(undefined);
                  }
                }}
                autoFocus
                flex={1}
              />
              <Button
                type="submit"
                size="xs"
                disabled={disabled || draft.trim() === ""}
              >
                Save
              </Button>
              <Button
                variant="subtle"
                size="xs"
                onClick={() => setDraft(undefined)}
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
