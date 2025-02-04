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
} from "../../../../test";
import { ADD_LINE_ITEM_BUTTON, SpendTable } from "./SpendTable";
import { LineItem } from "./types";
import {
  NAME_MAX_LENGTH,
  NAME_MAX_LENGTH_ERROR,
  NAME_REQUIRED_ERROR,
} from "./validation";

const data: LineItem[] = [
  {
    id: "1",
    name: "item1",
  },
  {
    id: "2",
    name: "item2",
  },
  {
    id: "3",
    name: "item3",
  },
  {
    id: "4",
    name: "item4",
  },
];

describe("SpendTable", async () => {
  test("renders headers", async () => {
    render(<SpendTable data={data} />);

    expect(screen.getByText(/Name/)).toBeInTheDocument();
  });

  test("renders rows", async () => {
    render(<SpendTable data={data} />);

    for (const entry of data) {
      expect(screen.getByText(entry.name)).toBeInTheDocument();
    }
  });

  test("adds line item", async () => {
    const name = "Added line item";
    const onAddLineItem = vi.fn();

    render(<SpendTable data={data} onAddLineItem={onAddLineItem} />);

    const addLineItemBtn = screen.getByText(ADD_LINE_ITEM_BUTTON);
    await userEvent.click(addLineItemBtn);

    const input = screen.getByLabelText(/Name/);
    fireEvent.change(input, { target: { value: name } });

    const saveBtn = screen.getByText(/Save/);
    await userEvent.click(saveBtn);

    expect(onAddLineItem).toHaveBeenCalledWith({ name });
  });

  test("line item name is required", async () => {
    render(<SpendTable data={data} />);

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

    render(<SpendTable data={data} onAddLineItem={onAddLineItem} />);

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

    expect(onAddLineItem).toHaveBeenCalledWith({ name });
  });
});
