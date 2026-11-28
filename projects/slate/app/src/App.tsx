import { useRef, useState } from "react";
import {
  AppShell,
  Box,
  Burger,
  Center,
  CloseButton,
  Group,
  Image,
  Kbd,
  Loader,
  NavLink,
  TextInput,
  Title,
} from "@mantine/core";
import { useDebouncedCallback, useHotkeys } from "@mantine/hooks";
import { Notifications } from "@mantine/notifications";
import { IconSearch } from "@tabler/icons-react";
// Inlined, it would be a data: URL, which the service's content security policy blocks.
import mark from "../../icons/slate-mark.svg?no-inline";
import { linkedItemId, useLinkedItem } from "./itemLink";
import { ListUpdates } from "./ListUpdates";
import { ItemsPage } from "./pages/ItemsPage";
import { type ViewName, views } from "./views";

const SEARCH_DELAY_MS = 250;
const MAX_SEARCH_LENGTH = 200;

export function App() {
  const [view, setView] = useState<ViewName>("today");
  const [linkedId, setLinkedId] = useState(linkedItemId);
  const [openId, setOpenId] = useState<string>();
  const linked = useLinkedItem(linkedId);
  if (linked !== undefined) {
    setLinkedId(undefined);
    if (linked.found) {
      setView(linked.view);
      setOpenId(linked.id);
    }
  }
  const [narrowNavOpened, setNarrowNavOpened] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [search, setSearch] = useState<string>();
  const searchLater = useDebouncedCallback(setSearch, SEARCH_DELAY_MS);
  const searchRef = useRef<HTMLInputElement>(null);
  useHotkeys([["mod+K", () => searchRef.current?.focus()]], []);

  function changeSearch(text: string) {
    setSearchText(text);
    setOpenId(undefined);
    if (text.trim() === "") {
      searchLater.cancel();
      setSearch(undefined);
    } else {
      searchLater(text.trim());
    }
  }

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
        <Group h="100%" px="lg" gap="xs" wrap="nowrap">
          <Group flex={1} gap="xs" wrap="nowrap">
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
          <TextInput
            ref={searchRef}
            type="search"
            w={{ base: 200, sm: 400 }}
            aria-label="Search every item"
            placeholder="Search every item"
            maxLength={MAX_SEARCH_LENGTH}
            leftSection={<IconSearch size={16} />}
            rightSectionWidth={searchText === "" ? 42 : undefined}
            rightSection={
              searchText === "" ? (
                <Kbd size="xs">⌘K</Kbd>
              ) : (
                <CloseButton
                  size="sm"
                  aria-label="Clear search"
                  onClick={() => {
                    changeSearch("");
                    searchRef.current?.focus();
                  }}
                />
              )
            }
            value={searchText}
            onChange={(event) => changeSearch(event.currentTarget.value)}
            onKeyDown={(event) => {
              if (event.key === "Escape") {
                changeSearch("");
              }
            }}
          />
          <Box flex={1} />
        </Group>
      </AppShell.Header>
      <AppShell.Navbar p="xs" aria-label="Views">
        {Object.entries(views).map(([name, { label }]) => (
          <NavLink
            key={name}
            component="button"
            label={label}
            active={search === undefined && name === view}
            aria-current={
              search === undefined && name === view ? "page" : undefined
            }
            onClick={() => {
              setView(name as ViewName);
              setOpenId(undefined);
              changeSearch("");
              setNarrowNavOpened(false);
            }}
          />
        ))}
      </AppShell.Navbar>
      <AppShell.Main bg="gray.0">
        {linkedId === undefined ? (
          <ListUpdates>
            <ItemsPage
              key={search === undefined ? view : "search"}
              view={view}
              search={search}
              initialOpenId={search === undefined ? openId : undefined}
            />
          </ListUpdates>
        ) : (
          <Center p="xl">
            <Loader aria-label="Opening the linked item" />
          </Center>
        )}
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
