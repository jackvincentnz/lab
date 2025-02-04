import { MockedResponse } from "@apollo/client/testing";
import {
  describe,
  expect,
  fireEvent,
  render,
  screen,
  test,
  userEvent,
} from "../../test";
import { SpendPage } from "./SpendPage";
import {
  AddLineItemDocument,
  AddLineItemMutation,
  AddLineItemMutationVariables,
  AllLineItemsDocument,
  AllLineItemsQuery,
} from "../../__generated__/graphql";
import { LineItem } from "./components/spend-table";
import { ADD_LINE_ITEM_BUTTON } from "./components/spend-table/SpendTable";

describe("SpendPage", async () => {
  test("renders table", async () => {
    const name = "My Line Item";
    const lineItems = mockAllLineItems({ names: [name] });

    render(<SpendPage />, {
      mockedProvider: {
        mocks: [lineItems],
      },
    });

    expect(await screen.findByText(/Name/)).toBeInTheDocument();
    expect(await screen.findByText(name)).toBeInTheDocument();
  });

  test("adds line item", async () => {
    const name = "My Line Item";
    const initial = mockAllLineItems();
    const mutation: MockedResponse<
      AddLineItemMutation,
      AddLineItemMutationVariables
    > = {
      request: {
        query: AddLineItemDocument,
        variables: { input: { name } },
      },
      result: {
        data: {
          addLineItem: {
            code: 201,
            success: true,
            message: "added",
            lineItem: mockLineItem({ name }),
          },
        },
      },
    };
    const refetch = mockAllLineItems({ names: [name] });

    render(<SpendPage />, {
      mockedProvider: {
        mocks: [initial, mutation, refetch],
      },
    });

    const addLineItemBtn = await screen.findByText(ADD_LINE_ITEM_BUTTON);
    await userEvent.click(addLineItemBtn);

    const input = await screen.findByLabelText(/Name/);
    fireEvent.change(input, { target: { value: name } });

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

function mockLineItem({ name }: Partial<LineItem>): LineItem {
  return {
    id: name ?? Date.now().toString(),
    name: name ?? Date.now().toString(),
  };
}
