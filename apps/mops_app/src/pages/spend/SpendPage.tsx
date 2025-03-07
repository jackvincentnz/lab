import { useQuery } from "@apollo/client";
import {
  AllLineItemsDocument,
  AllCategoriesDocument,
  AllLineItemsQuery,
  AllCategoriesQuery,
} from "../../__generated__/graphql";
import { Column, LineItem, Option, SpendTable } from "./components/spend-table";
import { useAddLineItemMutation } from "./mutations";
import { Field } from "./components/spend-table/types";

export function SpendPage() {
  const { data: lineItems } = useQuery(AllLineItemsDocument);
  const { data: categories } = useQuery(AllCategoriesDocument);

  const addLineItem = useAddLineItemMutation();
  const columns = mapToColumns(categories);

  return (
    <SpendTable
      columns={columns}
      lineItems={mapToLineItems(lineItems)}
      onAddLineItem={addLineItem}
    />
  );
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
    fields: lineItem.categorizations.map(mapToField),
  };
}

function mapToField(
  categorization: AllLineItemsQuery["allLineItems"][number]["categorizations"][number],
): Field {
  return {
    id: categorization.category.id,
    value: categorization.categoryValue.name,
  };
}

function mapToColumns(data?: AllCategoriesQuery): Column[] {
  return data?.allCategories.map(mapToColumn) || [];
}

function mapToColumn(
  category: AllCategoriesQuery["allCategories"][number],
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
  categoryValue: AllCategoriesQuery["allCategories"][number]["values"][number],
): Option {
  return {
    value: categoryValue.id,
    label: categoryValue.name,
  };
}
