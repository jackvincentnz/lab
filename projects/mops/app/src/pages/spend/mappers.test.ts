import { describe, expect, test } from "../../test";
import { Category, CategoryValue, LineItem } from "../../__generated__/graphql";
import { mapToColumns, mapToLineItems } from "./mappers";

describe("spend mappers", () => {
  test("maps query line items to table rows", () => {
    const lineItems = mapToLineItems([
      {
        ...mockLineItem({ name: "Office" }),
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
        name: "Office",
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
        name: "Office",
        fields: [{ id: "country", value: "New Zealand" }],
      }),
    ).toBe("New Zealand");
  });
});

function mockLineItem(overrides: Partial<LineItem>): LineItem {
  return {
    id: overrides.id ?? "line-item-1",
    budgetId: "budget-1",
    name: overrides.name ?? "Line Item",
    spending: [],
    spendTotals: {
      monthlyTotals: [],
      quarterlyTotals: [],
      annualTotals: [],
      grandTotal: 0,
    },
    categorizations: overrides.categorizations ?? [],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
}

function mockCategory(overrides: Partial<Category>): Category {
  return {
    id: overrides.id ?? "category-1",
    name: overrides.name ?? "Category",
    values: overrides.values ?? [mockCategoryValue({})],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
}

function mockCategoryValue(overrides: Partial<CategoryValue>): CategoryValue {
  return {
    id: overrides.id ?? "value-1",
    name: overrides.name ?? "Value",
  };
}
