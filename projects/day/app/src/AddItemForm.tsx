import { useState, type FormEvent } from "react";
import { Button, Group, Text, TextInput } from "@mantine/core";
import { useIsMutating, useMutation } from "@tanstack/react-query";
import { useTRPC } from "./api";

export function AddItemForm() {
  const trpc = useTRPC();
  const saving = useIsMutating() > 0;
  const [name, setName] = useState("");
  const addItem = useMutation(
    trpc.addItem.mutationOptions({ onSuccess: () => setName("") }),
  );

  function submit(event: FormEvent) {
    event.preventDefault();
    addItem.mutate({ name });
  }

  return (
    <form onSubmit={submit}>
      <Group align="flex-end" wrap="nowrap">
        <TextInput
          label="New item"
          value={name}
          onChange={(event) => setName(event.currentTarget.value)}
          flex={1}
        />
        <Button type="submit" disabled={saving || name.trim() === ""}>
          Add
        </Button>
      </Group>
      {addItem.isError && (
        <Text c="red" size="sm" mt="xs">
          Could not save your change
        </Text>
      )}
    </form>
  );
}
