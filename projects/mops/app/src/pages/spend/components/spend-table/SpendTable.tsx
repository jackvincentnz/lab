import classes from "./SpendTable.module.css";
import { useMemo } from "react";
import {
  createRow,
  MantineReactTable,
  useMantineReactTable,
} from "mantine-react-table";
import clsx from "clsx";
import { useAddLineItem } from "./actions";
import { Column, LineItem, NewLineItem } from "./types";
import { useLineItemValidation } from "./validation";
import { AddLineItemToolbar, createSpendTableColumns } from "./columns";

export interface SpendTableProps {
  columns: Column[];
  lineItems: LineItem[];
  onAddLineItem?: (lineItem: NewLineItem) => void;
  onDeleteAllLineItems?: () => void;
  loading?: boolean;
}

const NEW_ROW: LineItem = {
  id: "",
  name: "",
  fields: [],
};

export function SpendTable({
  columns,
  lineItems,
  onAddLineItem,
  loading,
}: SpendTableProps) {
  const {
    validationErrors,
    setValidationErrors,
    removeErrorsFor,
    clearErrors,
  } = useLineItemValidation();

  const columnDefs = useMemo(
    () => createSpendTableColumns(columns, validationErrors, removeErrorsFor),
    [columns, removeErrorsFor, validationErrors],
  );

  const table = useMantineReactTable({
    columns: columnDefs,
    data: lineItems,
    initialState: {
      density: "xs",
    },
    state: {
      isLoading: loading,
    },
    enableFacetedValues: true,
    enablePagination: false,
    onCreatingRowCancel: clearErrors,
    onCreatingRowSave: useAddLineItem(setValidationErrors, onAddLineItem),
    renderTopToolbarCustomActions: () => (
      <AddLineItemToolbar onAddRow={onAddRow} />
    ),
    mantineTableProps: {
      className: clsx(classes["table"]),
      highlightOnHover: false,
      striped: "odd",
      withColumnBorders: true,
      withRowBorders: true,
      withTableBorder: true,
    },
  });

  const onAddRow = () => {
    const row = createRow(table, NEW_ROW);
    table.setCreatingRow(row);
  };

  return <MantineReactTable table={table} />;
}
