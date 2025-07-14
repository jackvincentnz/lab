import { MockedResponse } from "@apollo/client/testing";
import {
  describe,
  expect,
  fireEvent,
  render,
  screen,
  test,
  userEvent,
  within,
} from "../../test";
import { SpendPage } from "./SpendPage";
import {
  AddLineItemDocument,
  AddLineItemMutation,
  AddLineItemMutationVariables,
  Category,
  CategoryValue,
  LineItem,
  SpendPageQueryDocument,
} from "../../__generated__/graphql";
import { ADD_LINE_ITEM_BUTTON } from "./components/spend-table/SpendTable";

describe("SpendPage", async () => {
  test("renders table", async () => {
    const name = "My Line Item";
    const mock = mockSpendPageQuery({ lineItemNames: [name] });

    render(<SpendPage />, {
      mockedProvider: {
        mocks: [mock],
      },
    });

    expect(await screen.findByText(/Name/)).toBeInTheDocument();
    expect(await screen.findByText(name)).toBeInTheDocument();
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

  test("adds line item", async () => {
    const name = "My Line Item";
    const categoryName = "Country";

    const initial = mockSpendPageQuery({ categoryNames: [categoryName] });
    const category = initial.result.data.allCategories[0];

    const mutation: MockedResponse<
      AddLineItemMutation,
      AddLineItemMutationVariables
    > = {
      request: {
        query: AddLineItemDocument,
        variables: {
          input: {
            name,
            budgetId: "",
            categorizations: [
              {
                categoryId: category.id,
                categoryValueId: category.values[0].id,
              },
            ],
          },
        },
      },
      result: {
        data: {
          addLineItem: {
            code: 201,
            success: true,
            message: "added",
            lineItem: mockLineItem({
              name,
              categorizations: [
                { category, categoryValue: category.values[0] },
              ],
            }),
          },
        },
      },
    };

    const refetch = mockSpendPageQuery({
      categoryNames: [categoryName],
      lineItemNames: [name],
    });

    render(<SpendPage />, {
      mockedProvider: {
        mocks: [initial, mutation, refetch],
      },
    });

    const addLineItemBtn = await screen.findByText(ADD_LINE_ITEM_BUTTON);
    await userEvent.click(addLineItemBtn);

    const input = await screen.findByLabelText(/Name/);
    fireEvent.change(input, { target: { value: name } });

    const modal = screen.getByRole("dialog");
    const categoryInput = within(modal).getByLabelText(category.name);

    await userEvent.click(categoryInput);

    const option = screen.getByText(category.values[0].name);
    await userEvent.click(option);

    const saveBtn = await screen.findByText(/Save/);
    await userEvent.click(saveBtn);

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    expect(await screen.findByText(name)).toBeInTheDocument();
  });
});

function mockSpendPageQuery(options?: {
  categoryNames?: string[];
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
          options?.categoryNames?.map((name) => mockCategory({ name })) ?? [],
        allLineItems:
          options?.lineItemNames?.map((name) => mockLineItem({ name })) ?? [],
      },
    },
  };
}

function mockLineItem({
  name,
  categorizations,
}: Partial<LineItem>): Omit<LineItem, "spendTotals"> {
  return {
    id: name ?? newId(),
    budgetId: newId(),
    name: name ?? newId(),
    spending: [],
    categorizations: categorizations || [],
  };
}

function mockCategory({ name }: Partial<Category>): Category {
  return {
    id: name ?? newId(),
    name: name ?? newId(),
    values: [mockCategoryValue()],
  };
}

function mockCategoryValue(): CategoryValue {
  return {
    id: newId(),
    name: newId(),
  };
}

function newId() {
  return Date.now().toString();
}
