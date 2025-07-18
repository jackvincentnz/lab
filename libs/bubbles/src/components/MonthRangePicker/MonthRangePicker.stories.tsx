import type { Meta, StoryObj } from "@storybook/react";

import { MonthRangePicker, MonthRangePickerProps } from "./MonthRangePicker";

const meta: Meta<typeof MonthRangePicker> = {
  title: "Components/MonthRangePicker",
  component: MonthRangePicker,
  tags: ["autodocs"],
  argTypes: {
    label: {
      control: { type: "text" },
    },
    variant: {
      options: [
        "filled",
        "light",
        "outline",
        "transparent",
        "white",
        "subtle",
        "default",
        "gradient",
      ],
      control: { type: "select" },
    },
    initialRange: { control: "object" },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Basic: Story = {};

const INITIAL_RANGE: MonthRangePickerProps["initialRange"] = [
  { year: 2025, month: 1 },
  { year: 2025, month: 12 },
];

export const InitialRange: Story = {
  args: {
    label: "Period",
    variant: "outline",
    initialRange: INITIAL_RANGE,
  },
};
