import {
  Select,
  TextInput,
  NumberInput,
  ActionIcon,
  Group,
} from "@mantine/core";
import { DateInput } from "@mantine/dates";
import { IconX } from "@tabler/icons-react";
import { FilterConditionProps } from "./types";

// Common operators for different field types
const OPERATORS = {
  string: [
    { value: "eq", label: "equals" },
    { value: "neq", label: "not equals" },
    { value: "contains", label: "contains" },
    { value: "startsWith", label: "starts with" },
    { value: "endsWith", label: "ends with" },
  ],
  number: [
    { value: "eq", label: "equals" },
    { value: "neq", label: "not equals" },
    { value: "gt", label: "greater than" },
    { value: "gte", label: "greater than or equal" },
    { value: "lt", label: "less than" },
    { value: "lte", label: "less than or equal" },
  ],
  date: [
    { value: "eq", label: "equals" },
    { value: "neq", label: "not equals" },
    { value: "gt", label: "after" },
    { value: "gte", label: "on or after" },
    { value: "lt", label: "before" },
    { value: "lte", label: "on or before" },
  ],
  select: [
    { value: "eq", label: "equals" },
    { value: "neq", label: "not equals" },
  ],
};

export function FilterConditionComponent({
  condition,
  fields,
  onUpdateCondition,
  onRemove,
}: FilterConditionProps) {
  const selectedField = fields.find((field) => field.name === condition.field);

  // Get available operators for the selected field
  const availableOperators = selectedField
    ? selectedField.operators.map((op) => {
        const operatorDef = OPERATORS[selectedField.type].find(
          (o) => o.value === op,
        );
        return operatorDef || { value: op, label: op };
      })
    : [];

  const handleFieldChange = (fieldName: string | null) => {
    if (!fieldName) return;

    const field = fields.find((f) => f.name === fieldName);
    if (!field) return;

    // Reset operator and value when field changes
    const defaultOperator = field.operators[0] || "eq";
    onUpdateCondition(condition.id, {
      field: fieldName,
      operator: defaultOperator,
      value: field.type === "number" ? 0 : field.type === "date" ? null : "",
    });
  };

  const handleOperatorChange = (operator: string | null) => {
    if (!operator) return;
    onUpdateCondition(condition.id, { operator });
  };

  const handleValueChange = (value: any) => {
    onUpdateCondition(condition.id, { value });
  };

  const renderValueInput = () => {
    if (!selectedField) return null;

    switch (selectedField.type) {
      case "string":
        return (
          <TextInput
            placeholder="Enter value"
            value={condition.value || ""}
            onChange={(event) => handleValueChange(event.currentTarget.value)}
            style={{ width: 200 }}
          />
        );

      case "number":
        return (
          <NumberInput
            placeholder="Enter number"
            value={condition.value || 0}
            onChange={handleValueChange}
            style={{ width: 200 }}
          />
        );

      case "date":
        return (
          <DateInput
            placeholder="Select date"
            value={condition.value}
            onChange={handleValueChange}
            style={{ width: 200 }}
          />
        );

      case "select":
        return (
          <Select
            placeholder="Select value"
            data={selectedField.options || []}
            value={condition.value}
            onChange={handleValueChange}
            style={{ width: 200 }}
          />
        );

      default:
        return null;
    }
  };

  return (
    <Group gap="sm" wrap="nowrap">
      <Select
        placeholder="Select field"
        data={fields.map((field) => ({
          value: field.name,
          label: field.label,
        }))}
        value={condition.field}
        onChange={handleFieldChange}
        style={{ width: 150 }}
      />

      <Select
        placeholder="Select operator"
        data={availableOperators}
        value={condition.operator}
        onChange={handleOperatorChange}
        disabled={!selectedField}
        style={{ width: 180 }}
      />

      {renderValueInput()}

      <ActionIcon
        variant="subtle"
        color="red"
        onClick={() => onRemove(condition.id)}
        aria-label="Remove condition"
      >
        <IconX size={16} />
      </ActionIcon>
    </Group>
  );
}
