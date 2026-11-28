import {
  ActionIcon,
  Anchor,
  Group,
  Select,
  Stack,
  Text,
  Title,
} from "@mantine/core";
import { useState } from "react";
import { IconX } from "@tabler/icons-react";
import type { Item } from "../api";
import { StatusBadge } from "./StatusBadge";

interface RelatedItemsProps {
  related: Item[];
  candidates: Item[];
  openableIds: string[];
  disabled: boolean;
  onOpen(id: string): void;
  onRelate(relatedId: string): void;
  onUnrelate(relatedId: string): void;
}

export function RelatedItems({
  related,
  candidates,
  openableIds,
  disabled,
  onOpen,
  onRelate,
  onUnrelate,
}: RelatedItemsProps) {
  const [search, setSearch] = useState("");

  return (
    <Stack gap="xs">
      <Title order={3} size="xs" c="dimmed" tt="uppercase">
        Related items
      </Title>
      {related.map((relatedItem) => (
        <Group key={relatedItem.id} gap="xs" wrap="nowrap">
          <StatusBadge status={relatedItem.status} />
          {openableIds.includes(relatedItem.id) ? (
            <Anchor
              component="button"
              size="sm"
              ta="start"
              flex={1}
              onClick={() => onOpen(relatedItem.id)}
            >
              {relatedItem.name}
            </Anchor>
          ) : (
            <Text size="sm" flex={1}>
              {relatedItem.name}
            </Text>
          )}
          <ActionIcon
            variant="subtle"
            color="gray"
            size="sm"
            aria-label={`Remove ${relatedItem.name} from related items`}
            disabled={disabled}
            onClick={() => onUnrelate(relatedItem.id)}
          >
            <IconX size={14} />
          </ActionIcon>
        </Group>
      ))}
      <Select
        aria-label="Add a related item"
        placeholder="Add a related item"
        size="xs"
        searchable
        selectFirstOptionOnChange
        searchValue={search}
        onSearchChange={setSearch}
        value={null}
        disabled={disabled}
        data={candidates.map((candidate) => ({
          value: candidate.id,
          label: candidate.name,
        }))}
        onChange={(relatedId) => {
          if (relatedId !== null) {
            setSearch("");
            onRelate(relatedId);
          }
        }}
      />
    </Stack>
  );
}
