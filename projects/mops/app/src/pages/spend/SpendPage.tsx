import { useQuery } from "@apollo/client";
import { SpendPageQueryDocument } from "../../__generated__/graphql";
import { SpendTable } from "./components/spend-table";
import {
  useAddLineItemMutation,
  useDeleteAllLineItemsMutation,
} from "./mutations";
import { mapToColumns, mapToLineItems } from "./mappers";

export function SpendPage() {
  const { data, loading } = useQuery(SpendPageQueryDocument);

  const addLineItem = useAddLineItemMutation(data?.allBudgets[0]?.id || "");
  const deleteAllLineItems = useDeleteAllLineItemsMutation();
  const columns = mapToColumns(data?.allCategories);

  return (
    <SpendTable
      columns={columns}
      lineItems={mapToLineItems(data?.allLineItems)}
      onAddLineItem={addLineItem}
      onDeleteAllLineItems={deleteAllLineItems}
      loading={loading}
    />
  );
}
