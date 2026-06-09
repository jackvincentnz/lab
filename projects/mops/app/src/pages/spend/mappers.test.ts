import { describe, expect, test } from "../../test";
import type { SpendPageQueryQuery } from "../../__generated__/graphql";
import { mapToColumns, mapToLineItems } from "./mappers";

type Category = SpendPageQueryQuery["allCategories"][number];
type CategoryValue = Category["values"][number];
type LineItem = SpendPageQueryQuery["allLineItems"][number];

describe("spend mappers", () => {
  test("maps query line items to table rows", () => {
    const lineItems = mapToLineItems([
      {
        ...mockLineItem({ name: "Webinar" }),
        categorizations: [
          {
            category: mockCategory({ id: "category-1", name: "Country" }),
            categoryValue: mockCategoryValue({
              id: "value-1",
              name: "NZ",
            }),
          },
        ],
      },
    ]);

    expect(lineItems).toEqual([
      {
        id: expect.any(String),
        name: "Webinar",
        fields: [{ id: "category-1", value: "NZ" }],
      },
    ]);
  });

  test("maps categories to spend table columns", () => {
    const [column] = mapToColumns([
      {
        ...mockCategory({ id: "country", name: "Country" }),
        values: [mockCategoryValue({ id: "nz", name: "New Zealand" })],
      },
    ]);

    expect(column.id).toBe("country");
    expect(column.header).toBe("Country");
    expect(column.options).toEqual([{ value: "nz", label: "New Zealand" }]);
    expect(
      column.accessor({
        id: "row-1",
        name: "Webinar",
        fields: [{ id: "country", value: "New Zealand" }],
      }),
    ).toBe("New Zealand");
  });
});

function mockLineItem(overrides: Partial<LineItem>): LineItem {
  return {
    id: overrides.id ?? "line-item-1",
    name: overrides.name ?? "Line Item",
    spending: [],
    categorizations: overrides.categorizations ?? [],
  };
}

function mockCategory(overrides: Partial<Category>): Category {
  return {
    id: overrides.id ?? "category-1",
    name: overrides.name ?? "Category",
    values: overrides.values ?? [mockCategoryValue({})],
  };
}

function mockCategoryValue(overrides: Partial<CategoryValue>): CategoryValue {
  return {
    id: overrides.id ?? "value-1",
    name: overrides.name ?? "Value",
  };
}
