import {
  describe,
  expect,
  fireEvent,
  randomString,
  render,
  screen,
  test,
  userEvent,
  vi,
  within,
} from "../../../../test";
import { ADD_LINE_ITEM_BUTTON, SpendTable } from "./SpendTable";
import { Column, LineItem } from "./types";
import {
  NAME_MAX_LENGTH,
  NAME_MAX_LENGTH_ERROR,
  NAME_REQUIRED_ERROR,
} from "./validation";

const columns: Column[] = ["column1", "column2"].map((column) => ({
  id: column,
  header: column,
  options: [{ value: column + "-value", label: column + "-label" }],
  accessor: (lineItem) =>
    lineItem.fields.find((attribute) => attribute.id === column)?.value,
}));

const lineItems: LineItem[] = [
  {
    id: "1",
    name: "item1",
    fields: [
      {
        id: columns[0].id,
        value: "value1",
      },
    ],
  },
  {
    id: "2",
    name: "item2",
    fields: [
      {
        id: columns[1].id,
        value: "value2",
      },
    ],
  },
];

describe("SpendTable", async () => {
  test("renders static headers", async () => {
    render(<SpendTable columns={[]} lineItems={[]} />);

    expect(screen.getByText(/Name/)).toBeInTheDocument();
  });

  test("renders dynamic headers", async () => {
    render(<SpendTable columns={columns} lineItems={[]} />);

    for (const column of columns) {
      expect(screen.getByText(column.header)).toBeInTheDocument();
    }
  });

  test("renders rows", async () => {
    render(<SpendTable columns={columns} lineItems={lineItems} />);

    for (const entry of lineItems) {
      expect(screen.getByText(entry.name)).toBeInTheDocument();

      if (entry.fields.length > 0) {
        expect(screen.getByText(entry.fields[0].value)).toBeInTheDocument();
      }
    }
  });

  test("adds line item", async () => {
    const name = "Added line item";
    const onAddLineItem = vi.fn();

    render(
      <SpendTable
        columns={columns}
        lineItems={[]}
        onAddLineItem={onAddLineItem}
      />,
    );

    const addLineItemBtn = screen.getByText(ADD_LINE_ITEM_BUTTON);
    await userEvent.click(addLineItemBtn);

    const nameInput = screen.getByLabelText(/Name/);
    fireEvent.change(nameInput, { target: { value: name } });

    const modal = screen.getByRole("dialog");
    const categoryInput = within(modal).getByLabelText(columns[0].header);

    await userEvent.click(categoryInput);

    const option = screen.getByText(columns[0].options[0].label);
    await userEvent.click(option);

    const saveBtn = screen.getByText(/Save/);
    await userEvent.click(saveBtn);

    expect(onAddLineItem).toHaveBeenCalledWith({
      name,
      fields: [{ id: columns[0].id, value: columns[0].options[0].value }],
    });
  });

  test("line item name is required", async () => {
    render(<SpendTable columns={columns} lineItems={lineItems} />);

    const addLineItemBtn = screen.getByText(ADD_LINE_ITEM_BUTTON);
    await userEvent.click(addLineItemBtn);

    const saveBtn = screen.getByText(/Save/);
    await userEvent.click(saveBtn);

    const errorMessage = screen.getByText(NAME_REQUIRED_ERROR);
    expect(errorMessage).toBeInTheDocument();
  });

  test("line item name has max length", async () => {
    const invalidName = randomString(NAME_MAX_LENGTH + 1);
    const name = randomString(NAME_MAX_LENGTH);
    const onAddLineItem = vi.fn();

    render(
      <SpendTable
        columns={columns}
        lineItems={lineItems}
        onAddLineItem={onAddLineItem}
      />,
    );

    const addLineItemBtn = screen.getByText(ADD_LINE_ITEM_BUTTON);
    await userEvent.click(addLineItemBtn);

    const input = screen.getByLabelText(/Name/);
    fireEvent.change(input, { target: { value: invalidName } });

    const saveBtn = screen.getByText(/Save/);
    await userEvent.click(saveBtn);

    const errorMessage = screen.getByText(NAME_MAX_LENGTH_ERROR);
    expect(errorMessage).toBeInTheDocument();
    expect(onAddLineItem).not.toHaveBeenCalled();

    await userEvent.clear(input);
    fireEvent.change(input, { target: { value: name } });
    await userEvent.click(saveBtn);

    expect(onAddLineItem).toHaveBeenCalledWith({ name, fields: [] });
  });
});
