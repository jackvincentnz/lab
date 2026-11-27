import type { ReactNode } from "react";
import { Title } from "@mantine/core";

interface ListHeadingProps {
  children: ReactNode;
}

export function ListHeading({ children }: ListHeadingProps) {
  return (
    <Title order={2} size="md" fw={600} lh="md">
      {children}
    </Title>
  );
}
