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
  AllCategoriesDocument,
  AllLineItemsDocument,
  AllLineItemsQuery,
  Category,
  CategoryValue,
  LineItem,
} from "../../__generated__/graphql";
import { ADD_LINE_ITEM_BUTTON } from "./components/spend-table/SpendTable";

describe("SpendPage", async () => {
  test("renders table", async () => {
    const name = "My Line Item";
    const categories = mockAllCategories();
    const lineItems = mockAllLineItems({ names: [name] });

    render(<SpendPage />, {
      mockedProvider: {
        mocks: [lineItems, categories],
      },
    });

    expect(await screen.findByText(/Name/)).toBeInTheDocument();
    expect(await screen.findByText(name)).toBeInTheDocument();
  });

  test("renders table columns", async () => {
    const category = "Country";
    const categories = mockAllCategories({ names: [category] });
    const lineItems = mockAllLineItems();

    render(<SpendPage />, {
      mockedProvider: {
        mocks: [categories, lineItems],
      },
    });

    expect(await screen.findByText(category)).toBeInTheDocument();
  });

  test("adds line item", async () => {
    const name = "My Line Item";
    const categoryName = "Country";

    const categories = mockAllCategories({ names: [categoryName] });
    const category = categories.result.data.allCategories[0];

    const initial = mockAllLineItems();

    const mutation: MockedResponse<
      AddLineItemMutation,
      AddLineItemMutationVariables
    > = {
      request: {
        query: AddLineItemDocument,
        variables: {
          input: {
            name,
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

    const refetch = mockAllLineItems({ names: [name] });

    render(<SpendPage />, {
      mockedProvider: {
        mocks: [categories, initial, mutation, refetch],
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

function mockAllLineItems(options?: {
  names?: string[];
}): MockedResponse<AllLineItemsQuery> {
  return {
    request: {
      query: AllLineItemsDocument,
    },
    result: {
      data: {
        allLineItems:
          options?.names?.map((name) => mockLineItem({ name })) ?? [],
      },
    },
  };
}

function mockLineItem({ name, categorizations }: Partial<LineItem>): LineItem {
  return {
    id: name ?? Date.now().toString(),
    name: name ?? Date.now().toString(),
    spending: [],
    categorizations: categorizations || [],
  };
}

function mockAllCategories(options?: { names?: string[] }) {
  return {
    request: {
      query: AllCategoriesDocument,
    },
    result: {
      data: {
        allCategories:
          options?.names?.map((name) => mockCategory({ name })) ?? [],
      },
    },
  };
}

function mockCategory({ name }: Partial<Category>): Category {
  return {
    id: name ?? Date.now().toString(),
    name: name ?? Date.now().toString(),
    values: [mockCategoryValue()],
  };
}

function mockCategoryValue(): CategoryValue {
  return {
    id: Date.now().toString(),
    name: Date.now().toString(),
  };
}
