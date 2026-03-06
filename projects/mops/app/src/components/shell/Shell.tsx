import { PropsWithChildren, useState } from "react";
import { AppShell } from "@mantine/core";
import { useDisclosure, useSessionStorage } from "@mantine/hooks";
import { Navbar } from "./Navbar";
import { useLocation } from "react-router-dom";
import { Chat } from "../chat";
import { ShellHeader } from "./ShellHeader";
import { AsideResizeHandle } from "./AsideResizeHandle";
import { ASIDE_MIN_WIDTH, ASIDE_OPENED_KEY } from "./shellState";
import { getNavigationLabel } from "./navigation";

export function Shell({ children }: PropsWithChildren) {
  const [opened, { toggle }] = useDisclosure();
  const [asideOpened, setAsideOpened] = useSessionStorage({
    key: ASIDE_OPENED_KEY,
    defaultValue: false,
  });
  const [asideWidth, setAsideWidth] = useState(ASIDE_MIN_WIDTH);

  const location = useLocation();

  const pageTitle = getNavigationLabel(location.pathname);

  function toggleAside() {
    if (asideOpened) {
      setAsideOpened(false);
    } else {
      setAsideOpened(true);
    }
  }

  return (
    <AppShell
      layout="alt"
      header={{ height: 60 }}
      navbar={{ width: 300, breakpoint: "sm", collapsed: { mobile: !opened } }}
      aside={{
        width: asideWidth,
        breakpoint: "sm",
        collapsed: { desktop: !asideOpened, mobile: true },
      }}
      transitionDuration={0}
      padding="md"
    >
      <AppShell.Header>
        <ShellHeader
          opened={opened}
          pageTitle={pageTitle}
          asideOpened={asideOpened}
          onToggleNavbar={toggle}
          onToggleAside={toggleAside}
        />
      </AppShell.Header>

      <AppShell.Navbar>
        <Navbar opened={opened} onCloseClick={toggle} />
      </AppShell.Navbar>

      <AppShell.Main>{children}</AppShell.Main>

      <AppShell.Aside>
        <AsideResizeHandle
          asideWidth={asideWidth}
          setAsideWidth={setAsideWidth}
        />
        <Chat />
      </AppShell.Aside>
    </AppShell>
  );
}
