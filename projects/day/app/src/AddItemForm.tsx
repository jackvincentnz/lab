import { useState, type FormEvent } from "react";
import { Button, Group, TextInput } from "@mantine/core";

interface AddItemFormProps {
  disabled: boolean;
  onAdd(name: string): Promise<boolean>;
}

export function AddItemForm({ disabled, onAdd }: AddItemFormProps) {
  const [name, setName] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (await onAdd(name)) {
      setName("");
    }
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
        <Button type="submit" disabled={disabled || name.trim() === ""}>
          Add
        </Button>
      </Group>
    </form>
  );
}
