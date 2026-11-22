import { useCallback, useEffect, useState } from "react";
import {
  Alert,
  Button,
  Center,
  List,
  Loader,
  Stack,
  Text,
} from "@mantine/core";
import { AddItemForm } from "./AddItemForm";
import { useDayApi, type Item, type ItemStatus } from "./api";
import { ItemRow } from "./ItemRow";

export function ItemList() {
  const api = useDayApi();
  const [items, setItems] = useState<Item[]>();
  const [loadFailed, setLoadFailed] = useState(false);
  const [saveFailed, setSaveFailed] = useState(false);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    try {
      setItems(await api.listItems());
      setLoadFailed(false);
    } catch {
      setLoadFailed(true);
    }
  }, [api]);

  useEffect(() => {
    void load();
  }, [load]);

  async function save(change: () => Promise<Item>): Promise<boolean> {
    setSaving(true);
    try {
      await change();
    } catch {
      setSaveFailed(true);
      setSaving(false);
      return false;
    }
    setSaveFailed(false);
    // The service decides the order, so show its list rather than patching ours.
    await load();
    setSaving(false);
    return true;
  }

  function retry() {
    setLoadFailed(false);
    void load();
  }

  return (
    <Stack>
      <AddItemForm
        disabled={saving}
        onAdd={(name) => save(() => api.addItem(name))}
      />
      {loadFailed && (
        <Alert color="red" title="Could not load your list">
          <Button variant="light" color="red" size="xs" onClick={retry}>
            Retry
          </Button>
        </Alert>
      )}
      {saveFailed && <Alert color="red" title="Could not save your change" />}
      {items === undefined && !loadFailed && (
        <Center>
          <Loader aria-label="Loading your list" />
        </Center>
      )}
      {items?.length === 0 && (
        <Text c="dimmed">
          Nothing on your list yet. Add the first item above.
        </Text>
      )}
      {items !== undefined && items.length > 0 && (
        <List
          type="ordered"
          listStyleType="none"
          spacing="xs"
          aria-label="Items"
        >
          {items.map((item) => (
            <ItemRow
              key={item.id}
              item={item}
              disabled={saving}
              onRename={(name) =>
                save(() => api.updateItem({ id: item.id, name }))
              }
              onStatusChange={(status: ItemStatus) =>
                save(() => api.updateItem({ id: item.id, status }))
              }
            />
          ))}
        </List>
      )}
    </Stack>
  );
}
