import { describe, expect, render, screen, test } from "../../../../test";
import { SpendTable } from "./SpendTable";
import type { Column, LineItem } from "./types";

const columns: Column[] = ["column1", "column2"].map((column) => ({
  id: column,
  header: column,
  options: [{ value: column + "-value", label: column + "-label" }],
  accessor: (lineItem) =>
    lineItem.fields.find((attribute) => attribute.id === column)?.value,
}));

const lineItems: LineItem[] = [
  {
    id: "1",
    name: "item1",
    fields: [
      {
        id: columns[0].id,
        value: "value1",
      },
    ],
  },
  {
    id: "2",
    name: "item2",
    fields: [
      {
        id: columns[1].id,
        value: "value2",
      },
    ],
  },
];

describe("SpendTable", async () => {
  test("renders static headers", async () => {
    render(<SpendTable columns={[]} lineItems={[]} />);

    expect(screen.getByText(/Name/)).toBeInTheDocument();
  });

  test("renders dynamic headers", async () => {
    render(<SpendTable columns={columns} lineItems={[]} />);

    for (const column of columns) {
      expect(screen.getByText(column.header)).toBeInTheDocument();
    }
  });

  test("renders rows", async () => {
    render(<SpendTable columns={columns} lineItems={lineItems} />);

    for (const entry of lineItems) {
      expect(
        screen.getByRole("cell", { name: entry.name }),
      ).toBeInTheDocument();

      if (entry.fields.length > 0) {
        expect(
          screen.getByRole("cell", { name: entry.fields[0].value }),
        ).toBeInTheDocument();
      }
    }
  });
});
