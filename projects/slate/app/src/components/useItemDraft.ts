import { useState } from "react";
import type { Item } from "../api";
import { useHoldListUpdates } from "../ListUpdates";

export interface SaveCallbacks {
  onSaved(saved: Item): void;
  onChanged(): void;
}

export type SaveDraft<Value = string> = (
  editedFrom: Item,
  value: Value,
  callbacks: SaveCallbacks,
) => void;

export interface Draft<Value> {
  value: Value;
  editedFrom: Item;
  refusedFrom?: Item;
}

export function useItemDraft<Value>(
  item: Item,
  valueOf: (item: Item) => Value,
  save: SaveDraft<Value>,
) {
  const [draft, setDraft] = useState<Draft<Value>>();
  useHoldListUpdates(draft !== undefined);

  function start() {
    setDraft({ value: valueOf(item), editedFrom: item });
  }

  function change(value: Value) {
    setDraft((current) => current && { ...current, value });
  }

  function submit(value?: Value) {
    if (draft === undefined) {
      return;
    }
    const submitted = value ?? draft.value;
    // A save overwrites only its own field, so only that field changed elsewhere makes the draft stale.
    const fieldChangedElsewhere = !sameValue(
      valueOf(item),
      valueOf(draft.editedFrom),
    );
    const editedFrom =
      draft.refusedFrom !== undefined || !fieldChangedElsewhere
        ? item
        : draft.editedFrom;
    save(editedFrom, submitted, {
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

function sameValue(a: unknown, b: unknown) {
  return JSON.stringify(a) === JSON.stringify(b);
}
