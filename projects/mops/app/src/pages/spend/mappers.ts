import { SpendPageQueryQuery } from "../../__generated__/graphql";
import { Column, LineItem, Option } from "./components/spend-table";
import { Field } from "./components/spend-table/types";

export function mapToLineItems(
  allLineItems?: SpendPageQueryQuery["allLineItems"],
): LineItem[] {
  return allLineItems?.map(mapToLineItem) || [];
}

export function mapToLineItem(
  lineItem: SpendPageQueryQuery["allLineItems"][number],
): LineItem {
  return {
    id: lineItem.id,
    name: lineItem.name,
    fields: lineItem.categorizations.map(mapToField),
  };
}

export function mapToField(
  categorization: SpendPageQueryQuery["allLineItems"][number]["categorizations"][number],
): Field {
  return {
    id: categorization.category.id,
    value: categorization.categoryValue.name,
  };
}

export function mapToColumns(
  allCategories?: SpendPageQueryQuery["allCategories"],
): Column[] {
  return allCategories?.map(mapToColumn) || [];
}

export function mapToColumn(
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

export function mapToOption(
  categoryValue: SpendPageQueryQuery["allCategories"][number]["values"][number],
): Option {
  return {
    value: categoryValue.id,
    label: categoryValue.name,
  };
}
