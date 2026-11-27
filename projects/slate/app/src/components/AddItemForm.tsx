import { useState, type FormEvent } from "react";
import { TextInput } from "@mantine/core";
import { IconPlus } from "@tabler/icons-react";

interface AddItemFormProps {
  saving: boolean;
  onAdd(name: string, onSaved: () => void): void;
}

export function AddItemForm({ saving, onAdd }: AddItemFormProps) {
  const [name, setName] = useState("");

  function submit(event: FormEvent) {
    event.preventDefault();
    if (saving || name.trim() === "") {
      return;
    }
    onAdd(name, () => setName((current) => (current === name ? "" : current)));
  }

  return (
    <form onSubmit={submit}>
      <TextInput
        aria-label="New item"
        placeholder="Add an item and press Enter"
        leftSection={<IconPlus size={16} />}
        leftSectionPointerEvents="none"
        value={name}
        onChange={(event) => setName(event.currentTarget.value)}
      />
    </form>
  );
}
