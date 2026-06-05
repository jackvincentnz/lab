import classes from "./SpendTable.module.css";
import { useMemo } from "react";
import { DataTable } from "mantine-datatable";
import type { Column, LineItem } from "./types";
import { createSpendTableColumns } from "./columns";

export interface SpendTableProps {
  columns: Column[];
  lineItems: LineItem[];
  loading?: boolean;
}

export function SpendTable({ columns, lineItems, loading }: SpendTableProps) {
  const columnDefs = useMemo(() => createSpendTableColumns(columns), [columns]);

  return (
    <DataTable
      className={classes["table"]}
      columns={columnDefs}
      fetching={loading}
      highlightOnHover={false}
      minHeight={"120px"}
      noRecordsText="No line items"
      records={lineItems}
      striped
      withColumnBorders
      withTableBorder
    />
  );
}
