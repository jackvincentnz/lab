import classes from "./SpendTable.module.css";
import { useMemo } from "react";
import {
  MantineReactTable,
  type MRT_ColumnDef,
  useMantineReactTable,
} from "mantine-react-table";
import clsx from "clsx";
import { Button } from "@mantine/core";
import { useAddLineItem } from "./actions";
import { LineItem, NewLineItem } from "./types";
import { useLineItemValidation } from "./validation";

export const ADD_LINE_ITEM_BUTTON = "Add Line Item";
export const HEADER_NAME = "Name";

export interface SpendTableProps {
  data: LineItem[];
  onAddLineItem?: (lineItem: NewLineItem) => void;
}

export function SpendTable({ data, onAddLineItem }: SpendTableProps) {
  const {
    validationErrors,
    setValidationErrors,
    removeErrorsFor,
    clearErrors,
  } = useLineItemValidation();

  const columns = useMemo<MRT_ColumnDef<LineItem>[]>(
    () => [
      {
        accessorKey: "name",
        header: HEADER_NAME,
        mantineEditTextInputProps: {
          required: true,
          error: validationErrors?.["name"],
          onFocus: removeErrorsFor("name"),
        },
      },
    ],
    [validationErrors],
  );

  const table = useMantineReactTable({
    columns,
    data,
    enableColumnActions: false,
    enableColumnFilters: false,
    enablePagination: false,
    enableSorting: false,
    onCreatingRowCancel: clearErrors,
    onCreatingRowSave: useAddLineItem(setValidationErrors, onAddLineItem),
    renderTopToolbarCustomActions: ({ table }) => (
      <Button
        onClick={() => {
          table.setCreatingRow(true);
        }}
      >
        {ADD_LINE_ITEM_BUTTON}
      </Button>
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

  return <MantineReactTable table={table} />;
}
