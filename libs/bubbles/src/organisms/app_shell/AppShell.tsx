import { PropsWithChildren, useState } from "react";
import {
  AppShell as MAppShell,
  Navbar,
  Header,
  Text,
  MediaQuery,
  Burger,
  useMantineTheme,
} from "@mantine/core";
import { MainLinks } from "./MainLinks";
import { User } from "./User";
import { Logo } from "./Logo";

export interface AppShellProps {
  title: string;
}

export function AppShell({
  title,
  children,
}: PropsWithChildren<AppShellProps>) {
  const theme = useMantineTheme();
  const [opened, setOpened] = useState(false);
  return (
    <MAppShell
      styles={{
        main: {
          background: theme.colors.gray[0],
        },
      }}
      navbarOffsetBreakpoint="sm"
      asideOffsetBreakpoint="sm"
      navbar={
        <Navbar
          p="md"
          hiddenBreakpoint="sm"
          hidden={!opened}
          width={{ sm: 200, lg: 300 }}
        >
          <Navbar.Section grow>
            <MainLinks />
          </Navbar.Section>
          <Navbar.Section>
            <User />
          </Navbar.Section>
        </Navbar>
      }
      header={
        <Header height={{ base: 50, md: 70 }} p="md">
          <div
            style={{ display: "flex", alignItems: "center", height: "100%" }}
          >
            <MediaQuery largerThan="sm" styles={{ display: "none" }}>
              <Burger
                opened={opened}
                onClick={() => setOpened((o) => !o)}
                size="sm"
                color={theme.colors.gray[6]}
                mr="xl"
              />
            </MediaQuery>

            <Logo />
            <Text ml={-65}>{title}</Text>
          </div>
        </Header>
      }
    >
      {children}
    </MAppShell>
  );
}
