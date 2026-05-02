import { Burger, Button, Group, Title } from "@mantine/core";
import { IconSparkles } from "@tabler/icons-react";

export interface ShellHeaderProps {
  opened: boolean;
  pageTitle?: string;
  asideOpened: boolean;
  onToggleNavbar: () => void;
  onToggleAside: () => void;
}

export function ShellHeader({
  opened,
  pageTitle,
  asideOpened,
  onToggleNavbar,
  onToggleAside,
}: ShellHeaderProps) {
  return (
    <Group justify="space-between" h="100%" px="md">
      <Group>
        <Burger
          opened={opened}
          onClick={onToggleNavbar}
          hiddenFrom="sm"
          size="sm"
        />
        <Title order={2}>{pageTitle}</Title>
      </Group>
      <Button
        leftSection={<IconSparkles />}
        variant={asideOpened ? "filled" : "outline"}
        onClick={onToggleAside}
      >
        AI Chat
      </Button>
    </Group>
  );
}
