import { useState } from "react";
import { AppShell, Burger, Group, Image, NavLink, Title } from "@mantine/core";
import { Notifications } from "@mantine/notifications";
// Inlined, it would be a data: URL, which the service's content security policy blocks.
import mark from "../../icons/slate-mark.svg?no-inline";
import { ListUpdates } from "./ListUpdates";
import { ItemsPage } from "./pages/ItemsPage";
import { type ViewName, views } from "./views";

export function App() {
  const [view, setView] = useState<ViewName>("today");
  const [narrowNavOpened, setNarrowNavOpened] = useState(false);

  return (
    <AppShell
      header={{ height: 56 }}
      navbar={{
        width: 200,
        breakpoint: "sm",
        collapsed: { mobile: !narrowNavOpened },
      }}
    >
      <AppShell.Header>
        <Group h="100%" px="lg" gap="xs">
          <Burger
            opened={narrowNavOpened}
            onClick={() => setNarrowNavOpened((opened) => !opened)}
            hiddenFrom="sm"
            size="sm"
            aria-label={narrowNavOpened ? "Hide views" : "Show views"}
          />
          <Image src={mark} alt="" w={28} h={28} />
          <Title order={1} fz="lg" fw={700}>
            Slate
          </Title>
        </Group>
      </AppShell.Header>
      <AppShell.Navbar p="xs" aria-label="Views">
        {Object.entries(views).map(([name, { label }]) => (
          <NavLink
            key={name}
            component="button"
            label={label}
            active={name === view}
            aria-current={name === view ? "page" : undefined}
            onClick={() => {
              setView(name as ViewName);
              setNarrowNavOpened(false);
            }}
          />
        ))}
      </AppShell.Navbar>
      <AppShell.Main bg="gray.0">
        <ListUpdates>
          <ItemsPage key={view} view={view} />
        </ListUpdates>
      </AppShell.Main>
      {/* Narrow enough to stay clear of the side-by-side item panel at its widest, 60% of the window. */}
      <Notifications
        position="bottom-left"
        layout="stacked"
        containerWidth={360}
      />
    </AppShell>
  );
}
