import { useQuery } from "@apollo/client";
import {
  AllLineItemsDocument,
  AllLineItemsQuery,
} from "../../__generated__/graphql";
import { LineItem, SpendTable } from "./components/spend-table";
import { useAddLineItemMutation } from "./mutations";

export function SpendPage() {
  const { data } = useQuery(AllLineItemsDocument);
  const addLineItem = useAddLineItemMutation();

  return <SpendTable data={mapToLineItems(data)} onAddLineItem={addLineItem} />;
}

function mapToLineItems(data?: AllLineItemsQuery): LineItem[] {
  return data?.allLineItems.map(mapToLineItem) || [];
}

function mapToLineItem(
  lineItem: AllLineItemsQuery["allLineItems"][number],
): LineItem {
  return {
    id: lineItem.id,
    name: lineItem.name,
  };
}
