import type { Meta, StoryObj } from "@storybook/react";

import { Shell } from "./Shell";

const meta: Meta<typeof Shell> = {
  title: "Organisms/Shell",
  component: Shell,
  parameters: {
    layout: "fullscreen",
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    title: "Page title",
  },
};
