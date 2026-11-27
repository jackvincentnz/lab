import { useState } from "react";
import { Badge, Box, Button, Menu } from "@mantine/core";
import { IconChevronDown } from "@tabler/icons-react";
import type { ItemStatus } from "../api";

interface StatusStyle {
  label: string;
  color: string;
}

const statuses: Record<ItemStatus, StatusStyle> = {
  todo: { label: "To do", color: "gray" },
  in_progress: { label: "In progress", color: "blue" },
  done: { label: "Done", color: "green" },
  deferred: { label: "Deferred", color: "yellow" },
  wont_do: { label: "Won't do", color: "red" },
  delegated: { label: "Delegated", color: "violet" },
};

interface StatusMenuProps {
  status: ItemStatus;
  disabled: boolean;
  onChange(status: ItemStatus): void;
}

export function StatusMenu({ status, disabled, onChange }: StatusMenuProps) {
  const { label, color } = statuses[status];
  const [opened, setOpened] = useState(false);

  return (
    <Box
      onKeyDown={(event) => {
        if (event.key === "Escape" && opened) {
          event.preventDefault();
        }
      }}
    >
      <Menu position="bottom-start" opened={opened} onChange={setOpened}>
        <Menu.Target>
          <Button
            size="xs"
            color={color}
            tt="uppercase"
            rightSection={<IconChevronDown size={14} />}
            aria-label={`Status: ${label}`}
          >
            {label}
          </Button>
        </Menu.Target>
        <Menu.Dropdown>
          {Object.entries(statuses)
            .filter(([other]) => other !== status)
            .map(([other, { label: otherLabel, color: otherColor }]) => (
              <Menu.Item
                key={other}
                disabled={disabled}
                onClick={() => onChange(other as ItemStatus)}
              >
                <Badge variant="light" color={otherColor} radius="sm">
                  {otherLabel}
                </Badge>
              </Menu.Item>
            ))}
        </Menu.Dropdown>
      </Menu>
    </Box>
  );
}
