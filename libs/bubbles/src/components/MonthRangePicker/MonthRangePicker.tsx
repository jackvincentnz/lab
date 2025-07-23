import { Popover, Button } from "@mantine/core";
import { MonthPicker } from "@mantine/dates";
import { useDisclosure } from "@mantine/hooks";
import { useState } from "react";
import dayjs from "dayjs";
import { ButtonVariant as MButtonVariant } from "@mantine/core";

export type ButtonVariant = MButtonVariant;

export interface MonthRangePickerProps {
  label?: string;
  variant?: ButtonVariant;
  initialRange?: [MonthYear, MonthYear];
  onChange?: (range: [MonthYear, MonthYear]) => void;
}

export interface MonthYear {
  year: number;
  month: number; // 1-12
}

const DEFAULT_INITIAL_RANGE: [Date | null, Date | null] = [null, null];

function initialRangeToDateRange([start, end]: [MonthYear, MonthYear]): [
  Date,
  Date,
] {
  return [monthYearToDate(start), monthYearToDate(end)];
}

function monthYearToDate(monthYear: MonthYear): Date {
  return new Date(monthYear.year, monthYear.month - 1);
}

function dateToMonthYear(date: Date): MonthYear {
  return {
    year: date.getFullYear(),
    month: date.getMonth() + 1,
  };
}

function formatDateRange(start: Date | null, end: Date | null): string {
  if (!start || !end) {
    return "Select Range";
  }

  const startFormatted = dayjs(start).format("MMM 'YY");
  const endFormatted = dayjs(end).format("MMM 'YY");

  return start.getFullYear() === end.getFullYear() &&
    start.getMonth() === end.getMonth()
    ? startFormatted
    : `${startFormatted} - ${endFormatted}`;
}

export function MonthRangePicker({
  label,
  variant,
  initialRange,
  onChange,
}: MonthRangePickerProps) {
  const initial = initialRange
    ? initialRangeToDateRange(initialRange)
    : DEFAULT_INITIAL_RANGE;

  const [validValue, setValidValue] = useState(initial);
  const [internalValue, setInternalValue] = useState(initial);

  const [opened, { close, open }] = useDisclosure(false);

  function handleRangeChange([start, end]: [Date | null, Date | null]) {
    setInternalValue([start, end]);

    if (start && end) {
      setValidValue([start, end]);
      close();

      onChange && onChange([dateToMonthYear(start), dateToMonthYear(end)]);
    }
  }

  function handlePopoverChange(opened: boolean) {
    if (!opened) {
      setInternalValue(validValue); // reset back to valid
      close();
    }
  }

  const buttonText = formatDateRange(internalValue[0], internalValue[1]);

  return (
    <Popover opened={opened} onChange={handlePopoverChange} withArrow>
      <Popover.Target>
        <Button onClick={open} variant={opened ? "filled" : variant}>
          {label ? `${label}: ${buttonText}` : buttonText}
        </Button>
      </Popover.Target>
      <Popover.Dropdown>
        <MonthPicker
          type="range"
          value={internalValue}
          defaultDate={internalValue[0] || undefined}
          onChange={handleRangeChange}
          allowSingleDateInRange={true}
        />
      </Popover.Dropdown>
    </Popover>
  );
}
