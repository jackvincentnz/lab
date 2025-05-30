import { useQuery } from "@apollo/client";
import {
  SpendPageQueryDocument,
  SpendPageQueryQuery,
} from "../../__generated__/graphql";
import { Column, LineItem, Option, SpendTable } from "./components/spend-table";
import {
  useAddLineItemMutation,
  useDeleteAllLineItemsMutation,
} from "./mutations";
import { Field } from "./components/spend-table/types";

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

function mapToLineItems(
  allLineItems?: SpendPageQueryQuery["allLineItems"],
): LineItem[] {
  return allLineItems?.map(mapToLineItem) || [];
}

function mapToLineItem(
  lineItem: SpendPageQueryQuery["allLineItems"][number],
): LineItem {
  return {
    id: lineItem.id,
    name: lineItem.name,
    fields: lineItem.categorizations.map(mapToField),
  };
}

function mapToField(
  categorization: SpendPageQueryQuery["allLineItems"][number]["categorizations"][number],
): Field {
  return {
    id: categorization.category.id,
    value: categorization.categoryValue.name,
  };
}

function mapToColumns(
  allCategories?: SpendPageQueryQuery["allCategories"],
): Column[] {
  return allCategories?.map(mapToColumn) || [];
}

function mapToColumn(
  category: SpendPageQueryQuery["allCategories"][number],
): Column {
  return {
    id: category.id,
    header: category.name,
    options: category.values.map(mapToOption),
    accessor: (lineItem) =>
      lineItem.fields.find((attribute) => attribute.id === category.id)?.value,
  };
}

function mapToOption(
  categoryValue: SpendPageQueryQuery["allCategories"][number]["values"][number],
): Option {
  return {
    value: categoryValue.id,
    label: categoryValue.name,
  };
}
