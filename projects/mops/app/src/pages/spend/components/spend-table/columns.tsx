import { Button, Group } from "@mantine/core";
import { createMRTColumnHelper, type MRT_ColumnDef } from "mantine-react-table";
import { Column, LineItem } from "./types";

export const ADD_LINE_ITEM_BUTTON = "Add Line Item";
export const HEADER_NAME = "Name";

const columnHelper = createMRTColumnHelper<LineItem>();

export function createSpendTableColumns(
  columns: Column[],
  validationErrors: Partial<Record<keyof LineItem, string>>,
  onFocus: (field: keyof LineItem) => () => void,
): MRT_ColumnDef<LineItem>[] {
  return [
    columnHelper.accessor("name", {
      header: HEADER_NAME,
      filterVariant: "autocomplete",
      mantineEditTextInputProps: {
        required: true,
        error: validationErrors?.["name"],
        onFocus: onFocus("name"),
      },
    }),
    ...columns.map((column) =>
      columnHelper.accessor((lineItem) => column.accessor(lineItem), {
        id: column.id,
        header: column.header,
        editVariant: "select",
        filterVariant: "select",
        mantineEditSelectProps: {
          data: column.options,
        },
      }),
    ),
  ];
}

export function AddLineItemToolbar({ onAddRow }: { onAddRow: () => void }) {
  return (
    <Group gap="xs">
      <Button onClick={onAddRow}>{ADD_LINE_ITEM_BUTTON}</Button>
    </Group>
  );
}
