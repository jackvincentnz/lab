import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
  waitFor,
} from "@lab/test-utils";
import { MonthRangePicker, type MonthYear } from "./MonthRangePicker";

const JAN_TO_MAR_2024: [MonthYear, MonthYear] = [
  { year: 2024, month: 1 },
  { year: 2024, month: 3 },
];

describe("MonthRangePicker", () => {
  it("prompts for a range when no initial range is given", () => {
    render(<MonthRangePicker />);

    expect(
      screen.getByRole("button", { name: "Select Range" }),
    ).toBeInTheDocument();
  });

  it("prefixes the range with the label", () => {
    render(<MonthRangePicker label="Period" initialRange={JAN_TO_MAR_2024} />);

    expect(
      screen.getByRole("button", { name: "Period: Jan '24 - Mar '24" }),
    ).toBeInTheDocument();
  });

  it("shows a single month when the range starts and ends in it", () => {
    render(
      <MonthRangePicker
        initialRange={[
          { year: 2024, month: 5 },
          { year: 2024, month: 5 },
        ]}
      />,
    );

    expect(screen.getByRole("button", { name: "May '24" })).toBeInTheDocument();
  });

  it("reports and shows the range after both months are picked", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(
      <MonthRangePicker initialRange={JAN_TO_MAR_2024} onChange={onChange} />,
    );

    await user.click(screen.getByRole("button", { name: "Jan '24 - Mar '24" }));
    await user.click(await screen.findByRole("button", { name: "Apr" }));
    await user.click(screen.getByRole("button", { name: "Jun" }));

    expect(onChange).toHaveBeenCalledExactlyOnceWith([
      { year: 2024, month: 4 },
      { year: 2024, month: 6 },
    ]);
    expect(
      screen.getByRole("button", { name: "Apr '24 - Jun '24" }),
    ).toBeInTheDocument();
    await waitFor(() =>
      expect(
        screen.queryByRole("button", { name: "Apr" }),
      ).not.toBeInTheDocument(),
    );
  });

  it("restores the last full range when closed mid-selection", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(
      <MonthRangePicker initialRange={JAN_TO_MAR_2024} onChange={onChange} />,
    );

    await user.click(screen.getByRole("button", { name: "Jan '24 - Mar '24" }));
    await user.click(await screen.findByRole("button", { name: "Apr" }));

    expect(
      screen.getByRole("button", { name: "Select Range" }),
    ).toBeInTheDocument();

    await user.keyboard("{Escape}");

    expect(
      screen.getByRole("button", { name: "Jan '24 - Mar '24" }),
    ).toBeInTheDocument();
    expect(onChange).not.toHaveBeenCalled();
  });
});
