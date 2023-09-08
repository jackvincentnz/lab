import { IconChevronRight, IconChevronLeft } from "@tabler/icons-react"; // FIXME: how does this work without adding dep?
import {
  UnstyledButton,
  Group,
  Avatar,
  Text,
  Box,
  useMantineTheme,
  rem,
} from "@mantine/core";

export function User() {
  const theme = useMantineTheme();

  return (
    <Box
      sx={{
        paddingTop: theme.spacing.sm,
        borderTop: `${rem(1)} solid ${theme.colors.gray[2]}`,
      }}
    >
      <UnstyledButton
        sx={{
          display: "block",
          width: "100%",
          padding: theme.spacing.xs,
          borderRadius: theme.radius.sm,

          "&:hover": {
            backgroundColor: theme.colors.gray[0],
          },
        }}
      >
        <Group>
          <Avatar radius="xl" />
          <Box sx={{ flex: 1 }}>
            <Text size="sm" weight={500}>
              John Doe
            </Text>
            <Text color="dimmed" size="xs">
              john.doe@example.com
            </Text>
          </Box>

          {theme.dir === "ltr" ? (
            <IconChevronRight size={rem(18)} />
          ) : (
            <IconChevronLeft size={rem(18)} />
          )}
        </Group>
      </UnstyledButton>
    </Box>
  );
}
