import { PropsWithChildren, useState } from "react";
import { AppShell, Burger, Group, Title, Button, Box } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { Navbar, navigationItems } from "./Navbar";
import { useLocation } from "react-router-dom";
import { IconSparkles } from "@tabler/icons-react";
import { Chat } from "../chat";

const ASIDE_MIN_WIDTH = 400;
const ASIDE_MAX_WIDTH = 1000;

export function Shell({ children }: PropsWithChildren) {
  const [opened, { toggle }] = useDisclosure();
  const [asideOpened, { toggle: toggleAside }] = useDisclosure();
  const [asideWidth, setAsideWidth] = useState(ASIDE_MIN_WIDTH);

  const location = useLocation();

  const pageTitle = navigationItems.find(
    (item) => item.link === location.pathname,
  );

  function handleAsideResize(e: React.MouseEvent) {
    e.preventDefault();
    const startX = e.clientX;
    const startWidth = asideWidth;

    const handleMouseMove = (e: MouseEvent) => {
      const newWidth = startWidth - (e.clientX - startX);
      const constrainedWidth = Math.max(
        ASIDE_MIN_WIDTH,
        Math.min(ASIDE_MAX_WIDTH, newWidth),
      );
      setAsideWidth(constrainedWidth);
    };

    const handleMouseUp = () => {
      document.removeEventListener("mousemove", handleMouseMove);
      document.removeEventListener("mouseup", handleMouseUp);
    };

    document.addEventListener("mousemove", handleMouseMove);
    document.addEventListener("mouseup", handleMouseUp);
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
        <Group justify="space-between" h="100%" px="md">
          <Group>
            <Burger
              opened={opened}
              onClick={toggle}
              hiddenFrom="sm"
              size="sm"
            />
            <Title order={2}>{pageTitle?.label}</Title>
          </Group>
          <Button
            leftSection={<IconSparkles />}
            variant={asideOpened ? "filled" : "outline"}
            onClick={toggleAside}
          >
            AI Chat
          </Button>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar>
        <Navbar opened={opened} onCloseClick={toggle} />
      </AppShell.Navbar>

      <AppShell.Main>{children}</AppShell.Main>

      <AppShell.Aside>
        <Box
          onMouseDown={handleAsideResize}
          style={{
            position: "absolute",
            left: 0,
            top: 0,
            width: 4,
            height: "100%",
            cursor: "col-resize",
            borderLeft: "1px solid var(--mantine-color-gray-3)",
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.borderLeft =
              "5px solid var(--mantine-color-blue-5)";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.borderLeft =
              "1px solid var(--mantine-color-gray-3)";
          }}
        />
        <Chat />
      </AppShell.Aside>
    </AppShell>
  );
}
