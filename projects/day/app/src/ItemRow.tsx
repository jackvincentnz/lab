import { useState, type FormEvent } from "react";
import {
  Button,
  Group,
  List,
  NativeSelect,
  Text,
  TextInput,
} from "@mantine/core";
import { useIsMutating, useMutation } from "@tanstack/react-query";
import { useTRPC, type Item, type ItemStatus } from "./api";

const statusOptions = [
  { value: "todo", label: "To do" },
  { value: "doing", label: "Doing" },
  { value: "done", label: "Done" },
];

interface ItemRowProps {
  item: Item;
}

export function ItemRow({ item }: ItemRowProps) {
  const trpc = useTRPC();
  const saving = useIsMutating() > 0;
  const [draft, setDraft] = useState<string>();
  const rename = useMutation(
    trpc.updateItem.mutationOptions({ onSuccess: () => setDraft(undefined) }),
  );
  const changeStatus = useMutation(trpc.updateItem.mutationOptions());

  function submitRename(event: FormEvent) {
    event.preventDefault();
    if (draft !== undefined) {
      rename.mutate({ id: item.id, name: draft });
    }
  }

  function cancelRename() {
    rename.reset();
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
            changeStatus.mutate({
              id: item.id,
              status: event.currentTarget.value as ItemStatus,
            })
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
      {(rename.isError || changeStatus.isError) && (
        <Text c="red" size="sm" mt="xs">
          Could not save your change
        </Text>
      )}
    </List.Item>
  );
}
