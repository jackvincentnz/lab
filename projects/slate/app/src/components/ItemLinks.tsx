import {
  ActionIcon,
  Anchor,
  Group,
  Select,
  Stack,
  Text,
  Title,
} from "@mantine/core";
import { IconX } from "@tabler/icons-react";
import type { Item } from "../api";
import { StatusBadge } from "./StatusBadge";

interface ItemLinksProps {
  links: Item[];
  candidates: Item[];
  openableIds: string[];
  disabled: boolean;
  onOpen(id: string): void;
  onLink(linkedId: string): void;
  onUnlink(linkedId: string): void;
}

export function ItemLinks({
  links,
  candidates,
  openableIds,
  disabled,
  onOpen,
  onLink,
  onUnlink,
}: ItemLinksProps) {
  return (
    <Stack gap="xs">
      <Title order={3} size="xs" c="dimmed" tt="uppercase">
        Linked items
      </Title>
      {links.map((linked) => (
        <Group key={linked.id} gap="xs" wrap="nowrap">
          <StatusBadge status={linked.status} />
          {openableIds.includes(linked.id) ? (
            <Anchor
              component="button"
              size="sm"
              ta="start"
              flex={1}
              onClick={() => onOpen(linked.id)}
            >
              {linked.name}
            </Anchor>
          ) : (
            <Text size="sm" flex={1}>
              {linked.name}
            </Text>
          )}
          <ActionIcon
            variant="subtle"
            color="gray"
            size="sm"
            aria-label={`Unlink ${linked.name}`}
            disabled={disabled}
            onClick={() => onUnlink(linked.id)}
          >
            <IconX size={14} />
          </ActionIcon>
        </Group>
      ))}
      <Select
        aria-label="Link an item"
        placeholder="Link an item"
        size="xs"
        searchable
        value={null}
        disabled={disabled}
        data={candidates.map((candidate) => ({
          value: candidate.id,
          label: candidate.name,
        }))}
        onChange={(linkedId) => {
          if (linkedId !== null) {
            onLink(linkedId);
          }
        }}
      />
    </Stack>
  );
}
