import {
  Popover,
  Button,
  type ButtonVariant as MButtonVariant,
} from "@mantine/core";
import { MonthPicker, type DatesRangeValue } from "@mantine/dates";
import { useDisclosure } from "@mantine/hooks";
import { useState } from "react";
import dayjs from "dayjs";

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

type MonthRangeValue = DatesRangeValue<string>;

const DEFAULT_INITIAL_RANGE: MonthRangeValue = [null, null];

function initialRangeToDateRange([start, end]: [
  MonthYear,
  MonthYear,
]): MonthRangeValue {
  return [monthYearToDate(start), monthYearToDate(end)];
}

function monthYearToDate(monthYear: MonthYear): string {
  return dayjs()
    .year(monthYear.year)
    .month(monthYear.month - 1)
    .date(1)
    .format("YYYY-MM-DD");
}

function dateToMonthYear(date: string): MonthYear {
  const parsedDate = dayjs(date);

  return {
    year: parsedDate.year(),
    month: parsedDate.month() + 1,
  };
}

function formatDateRange(start: string | null, end: string | null): string {
  if (!start || !end) {
    return "Select Range";
  }

  const startFormatted = dayjs(start).format("MMM 'YY");
  const endFormatted = dayjs(end).format("MMM 'YY");

  return dayjs(start).isSame(end, "month")
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

  function handleRangeChange([start, end]: MonthRangeValue) {
    setInternalValue([start, end]);

    if (start && end) {
      setValidValue([start, end]);
      close();

      if (onChange) {
        onChange([dateToMonthYear(start), dateToMonthYear(end)]);
      }
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
