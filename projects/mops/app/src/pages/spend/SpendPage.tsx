import { useQuery } from "@apollo/client/react";
import { SpendPageQueryDocument } from "../../__generated__/graphql";
import { SpendTable } from "./components/spend-table";
import { mapToColumns, mapToLineItems } from "./mappers";

export function SpendPage() {
  const { data, loading } = useQuery(SpendPageQueryDocument);

  const columns = mapToColumns(data?.allCategories);

  return (
    <SpendTable
      columns={columns}
      lineItems={mapToLineItems(data?.allLineItems)}
      loading={loading}
    />
  );
}
