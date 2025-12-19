import classes from "./Table.module.css";
import { useMemo } from "react";
import {
  type MRT_ColumnDef,
  MRT_Table,
  useMantineReactTable,
} from "mantine-react-table";
import { data } from "./data";
import clsx from "clsx";

export interface Person {
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  state: string;
}

export function Table() {
  const columns = useMemo<MRT_ColumnDef<Person>[]>(
    () => [
      {
        accessorKey: "firstName",
        header: "First Name",
      },
      {
        accessorKey: "lastName",
        header: "Last Name",
      },
      {
        accessorKey: "address",
        header: "Address",
      },
      {
        accessorKey: "city",
        header: "City",
      },
      {
        accessorKey: "state",
        header: "State",
      },
    ],
    [],
  );

  const table = useMantineReactTable({
    columns,
    data,
    enableColumnActions: false,
    enableColumnFilters: false,
    enablePagination: false,
    enableSorting: false,
    mantineTableProps: {
      className: clsx(classes["table"]),
      highlightOnHover: false,
      striped: "odd",
      withColumnBorders: true,
      withRowBorders: true,
      withTableBorder: true,
    },
  });

  return <MRT_Table table={table} />;
}
