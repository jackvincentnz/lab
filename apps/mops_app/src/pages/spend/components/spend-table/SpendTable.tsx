import classes from "./SpendTable.module.css";
import { useMemo } from "react";
import {
  createMRTColumnHelper,
  createRow,
  MantineReactTable,
  type MRT_ColumnDef,
  useMantineReactTable,
} from "mantine-react-table";
import clsx from "clsx";
import { Button } from "@mantine/core";
import { useAddLineItem } from "./actions";
import { Column, LineItem, NewLineItem } from "./types";
import { useLineItemValidation } from "./validation";

export const ADD_LINE_ITEM_BUTTON = "Add Line Item";
export const HEADER_NAME = "Name";

export interface SpendTableProps {
  columns: Column[];
  lineItems: LineItem[];
  onAddLineItem?: (lineItem: NewLineItem) => void;
}

const columnHelper = createMRTColumnHelper<LineItem>();

const NEW_ROW: LineItem = {
  id: "",
  name: "",
  fields: [],
};

export function SpendTable({
  columns,
  lineItems,
  onAddLineItem,
}: SpendTableProps) {
  const {
    validationErrors,
    setValidationErrors,
    removeErrorsFor,
    clearErrors,
  } = useLineItemValidation();

  const getTextInputProps = (field: keyof LineItem) => ({
    required: true,
    error: validationErrors?.[field],
    onFocus: removeErrorsFor(field),
  });

  const columnDefs = useMemo<MRT_ColumnDef<LineItem>[]>(
    () => [
      columnHelper.accessor("name", {
        header: HEADER_NAME,
        mantineEditTextInputProps: getTextInputProps("name"),
      }),
      ...columns.map((column) =>
        columnHelper.accessor((lineItem) => column.accessor(lineItem), {
          id: column.id,
          header: column.header,
          editVariant: "select",
          mantineEditSelectProps: {
            data: column.options,
          },
        }),
      ),
    ],
    [validationErrors, columns],
  );

  const table = useMantineReactTable({
    columns: columnDefs,
    data: lineItems,
    enableColumnActions: false,
    enableColumnFilters: false,
    enablePagination: false,
    enableSorting: false,
    onCreatingRowCancel: clearErrors,
    onCreatingRowSave: useAddLineItem(setValidationErrors, onAddLineItem),
    renderTopToolbarCustomActions: () => (
      <Button onClick={onAddRow}>{ADD_LINE_ITEM_BUTTON}</Button>
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
