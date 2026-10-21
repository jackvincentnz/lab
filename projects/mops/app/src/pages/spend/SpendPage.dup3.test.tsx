import { describe, expect, render, screen, test } from "../../test";
import { SpendPage } from "./SpendPage";
import {
  SpendPageQueryDocument,
  type SpendPageQueryQuery,
} from "../../__generated__/graphql";

type Category = SpendPageQueryQuery["allCategories"][number];
type CategoryValue = Category["values"][number];
type LineItem = SpendPageQueryQuery["allLineItems"][number];

describe("SpendPage", async () => {
  test("renders table", async () => {
    const name = "My Line Item";
    const mock = mockSpendPageQuery({ lineItemNames: [name] });

    render(<SpendPage />, {
      mockedProvider: {
        mocks: [mock],
      },
    });

    expect(
      await screen.findByRole("columnheader", { name: /Name/ }),
    ).toBeInTheDocument();
    expect(await screen.findByRole("cell", { name: name })).toBeInTheDocument();
  });

  test("renders table columns", async () => {
    const category = "Country";
    const mock = mockSpendPageQuery({ categoryNames: [category] });

    render(<SpendPage />, {
      mockedProvider: {
        mocks: [mock],
      },
    });

    expect(await screen.findByText(category)).toBeInTheDocument();
  });
});

function mockSpendPageQuery(options?: {
  categoryNames?: string[];
  categoryValueNames?: string[];
  lineItemNames?: string[];
}) {
  return {
    request: {
      query: SpendPageQueryDocument,
    },
    result: {
      data: {
        allBudgets: [],
        allCategories:
          options?.categoryNames?.map((name, index) =>
            mockCategory({
              name,
              values: [
                mockCategoryValue({
                  name: options?.categoryValueNames?.[index],
                }),
              ],
            }),
          ) ?? [],
        allLineItems:
          options?.lineItemNames?.map((name) => mockLineItem({ name })) ?? [],
      },
    },
  };
}

function mockLineItem({ name, categorizations }: Partial<LineItem>): LineItem {
  return {
    id: name ?? newId(),
    name: name ?? newId(),
    spending: [],
    categorizations: categorizations || [],
  };
}

function mockCategory({ name, values }: Partial<Category>): Category {
  return {
    id: name ?? newId(),
    name: name ?? newId(),
    values: values ?? [mockCategoryValue({})],
  };
}

function mockCategoryValue(overrides: Partial<CategoryValue>): CategoryValue {
  return {
    id: overrides.id ?? newId(),
    name: overrides.name ?? newId(),
  };
}

function newId() {
  return Date.now().toString();
}
