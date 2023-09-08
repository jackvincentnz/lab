import type { Meta, StoryObj } from "@storybook/react";

import { AppShell } from "./AppShell";

const meta: Meta<typeof AppShell> = {
  title: "Organisms/AppShell",
  component: AppShell,
  parameters: {
    layout: "fullscreen",
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    title: "Application title",
  },
};
