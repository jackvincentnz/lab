import { useState, type FormEvent } from "react";
import { Button, Group, TextInput } from "@mantine/core";

interface AddItemFormProps {
  saving: boolean;
  onAdd(name: string, onSaved: () => void): void;
}

export function AddItemForm({ saving, onAdd }: AddItemFormProps) {
  const [name, setName] = useState("");

  function submit(event: FormEvent) {
    event.preventDefault();
    onAdd(name, () => setName((current) => (current === name ? "" : current)));
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
    </form>
  );
}
