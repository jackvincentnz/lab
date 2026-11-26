import { useState } from "react";
import type { Item } from "../api";

export interface SaveCallbacks {
  onSaved(saved: Item): void;
  onChanged(): void;
}

export type SaveDraft = (
  editedFrom: Item,
  value: string,
  callbacks: SaveCallbacks,
) => void;

interface Draft {
  value: string;
  editedFrom: Item;
  refusedFrom?: Item;
}

export function useItemDraft(
  item: Item,
  field: "name" | "description",
  save: SaveDraft,
) {
  const [draft, setDraft] = useState<Draft>();

  function start() {
    setDraft({ value: item[field], editedFrom: item });
  }

  function change(value: string) {
    setDraft((current) => current && { ...current, value });
  }

  function submit() {
    if (draft === undefined) {
      return;
    }
    // A save overwrites only its own field, so only that field changed elsewhere makes the draft stale.
    const fieldChangedElsewhere = item[field] !== draft.editedFrom[field];
    const editedFrom =
      draft.refusedFrom !== undefined || !fieldChangedElsewhere
        ? item
        : draft.editedFrom;
    save(editedFrom, draft.value, {
      onSaved: (saved) =>
        setDraft((current) =>
          current === undefined || current.value === draft.value
            ? undefined
            : { value: current.value, editedFrom: saved },
        ),
      onChanged: () =>
        setDraft(
          (current) => current && { ...current, refusedFrom: editedFrom },
        ),
    });
  }

  function cancel() {
    setDraft(undefined);
  }

  return { draft, start, change, submit, cancel };
}
