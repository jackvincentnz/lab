import { useState } from "react";
import { Box, Button, Menu } from "@mantine/core";
import { IconChevronDown } from "@tabler/icons-react";
import type { ItemStatus } from "../api";
import { everyStatus, statusStyles } from "../statuses";
import { StatusBadge } from "./StatusBadge";

interface StatusMenuProps {
  status: ItemStatus;
  disabled: boolean;
  onChange(status: ItemStatus): void;
}

export function StatusMenu({ status, disabled, onChange }: StatusMenuProps) {
  const { label, color } = statusStyles[status];
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
          {everyStatus
            .filter((other) => other !== status)
            .map((other) => (
              <Menu.Item
                key={other}
                disabled={disabled}
                onClick={() => onChange(other)}
              >
                <StatusBadge status={other} />
              </Menu.Item>
            ))}
        </Menu.Dropdown>
      </Menu>
    </Box>
  );
}
