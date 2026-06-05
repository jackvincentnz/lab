import type { DataTableColumn } from "mantine-datatable";
import type { Column, LineItem } from "./types";

export const HEADER_NAME = "Name";

export function createSpendTableColumns(
  columns: Column[],
): DataTableColumn<LineItem>[] {
  return [
    {
      accessor: "name",
      title: HEADER_NAME,
    },
    ...columns.map((column) => ({
      accessor: column.id,
      title: column.header,
      render: (lineItem: LineItem) => column.accessor(lineItem),
    })),
  ];
}
