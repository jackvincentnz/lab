import classes from "./Table.module.css";
import { useMemo } from "react";
import { DataTable, type DataTableColumn } from "mantine-datatable";
import { data } from "./data";

export interface Person {
  id: number;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  state: string;
}

export function Table() {
  const columns = useMemo<DataTableColumn<Person>[]>(
    () => [
      {
        accessor: "firstName",
        title: "First Name",
      },
      {
        accessor: "lastName",
        title: "Last Name",
      },
      {
        accessor: "address",
        title: "Address",
      },
      {
        accessor: "city",
        title: "City",
      },
      {
        accessor: "state",
        title: "State",
      },
    ],
    [],
  );

  return (
    <DataTable
      className={classes["table"]}
      columns={columns}
      highlightOnHover={false}
      idAccessor="id"
      records={data}
      striped
      withColumnBorders
      withTableBorder
    />
  );
}
