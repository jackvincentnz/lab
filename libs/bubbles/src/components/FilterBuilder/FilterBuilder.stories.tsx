import type { Meta, StoryObj } from "@storybook/react";
import { FilterBuilder } from "./FilterBuilder";
import { FieldDefinition, FilterState } from "./types";

const meta: Meta<typeof FilterBuilder> = {
  title: "Components/FilterBuilder",
  component: FilterBuilder,
  parameters: {
    layout: "padded",
  },
  tags: ["autodocs"],
};

export default meta;
type Story = StoryObj<typeof meta>;

// Sample field definitions for testing
const sampleFields: FieldDefinition[] = [
  {
    name: "status",
    label: "Status",
    type: "select",
    operators: ["eq", "neq"],
    options: [
      { label: "Open", value: "open" },
      { label: "In Progress", value: "in_progress" },
      { label: "Closed", value: "closed" },
    ],
  },
  {
    name: "priority",
    label: "Priority",
    type: "select",
    operators: ["eq", "neq"],
    options: [
      { label: "Low", value: "low" },
      { label: "Medium", value: "medium" },
      { label: "High", value: "high" },
      { label: "Critical", value: "critical" },
    ],
  },
  {
    name: "assignee",
    label: "Assignee",
    type: "string",
    operators: ["eq", "neq", "contains"],
  },
  {
    name: "createdDate",
    label: "Created Date",
    type: "date",
    operators: ["eq", "gt", "gte", "lt", "lte"],
  },
  {
    name: "score",
    label: "Score",
    type: "number",
    operators: ["eq", "neq", "gt", "gte", "lt", "lte"],
  },
  {
    name: "title",
    label: "Title",
    type: "string",
    operators: ["eq", "neq", "contains", "startsWith", "endsWith"],
  },
];

// Sample initial state for complex example
const complexInitialState: FilterState = {
  id: "root",
  combinator: "OR",
  rules: [
    {
      id: "group-1",
      combinator: "AND",
      rules: [
        { id: "cond-1", field: "status", operator: "eq", value: "open" },
        { id: "cond-2", field: "priority", operator: "eq", value: "high" },
      ],
    },
    { id: "cond-3", field: "assignee", operator: "eq", value: "john.doe" },
  ],
};

export const Default: Story = {
  args: {
    fields: sampleFields,
    onChange: (state) => {
      console.log("Filter state changed:", JSON.stringify(state, null, 2));
    },
  },
};

export const WithInitialState: Story = {
  args: {
    fields: sampleFields,
    initialState: complexInitialState,
    onChange: (state) => {
      console.log("Filter state changed:", JSON.stringify(state, null, 2));
    },
  },
};

export const MinimalFields: Story = {
  args: {
    fields: [
      {
        name: "name",
        label: "Name",
        type: "string",
        operators: ["eq", "contains"],
      },
      {
        name: "active",
        label: "Active",
        type: "select",
        operators: ["eq"],
        options: [
          { label: "Yes", value: "true" },
          { label: "No", value: "false" },
        ],
      },
    ],
    onChange: (state) => {
      console.log("Filter state changed:", JSON.stringify(state, null, 2));
    },
  },
};

export const AllFieldTypes: Story = {
  args: {
    fields: sampleFields,
    initialState: {
      id: "root",
      combinator: "AND",
      rules: [
        {
          id: "string-example",
          field: "title",
          operator: "contains",
          value: "bug",
        },
        { id: "number-example", field: "score", operator: "gte", value: 85 },
        {
          id: "date-example",
          field: "createdDate",
          operator: "gt",
          value: new Date("2024-01-01"),
        },
        {
          id: "select-example",
          field: "status",
          operator: "eq",
          value: "open",
        },
      ],
    },
    onChange: (state) => {
      console.log("Filter state changed:", JSON.stringify(state, null, 2));
    },
  },
};
