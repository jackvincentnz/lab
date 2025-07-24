import {
  ActionIcon,
  Box,
  Button,
  Group,
  SegmentedControl,
  Stack,
  Paper,
  Flex,
} from "@mantine/core";
import { IconPlus, IconX } from "@tabler/icons-react";
import { FilterCondition, FilterGroup, FilterGroupProps } from "./types";
import { FilterConditionComponent } from "./FilterConditionComponent";

export function FilterGroupComponent({
  group,
  fields,
  onUpdateGroup,
  onAddCondition,
  onAddGroup,
  onRemove,
  depth = 0,
}: FilterGroupProps) {
  const isRoot = depth === 0;

  // Progressive background colors for nested groups
  const getBackgroundColor = (depth: number) => {
    if (depth === 0) return "transparent";
    const colors = ["gray.0", "gray.1", "gray.2", "blue.0", "blue.1"];
    return colors[Math.min(depth - 1, colors.length - 1)];
  };

  const handleCombinatorChange = (value: string) => {
    if (value === "AND" || value === "OR") {
      onUpdateGroup(group.id, { combinator: value });
    }
  };

  const renderRule = (rule: FilterCondition | FilterGroup) => {
    // Check if it's a FilterCondition (has 'field' property) or FilterGroup (has 'combinator' property)
    if ("field" in rule) {
      // It's a FilterCondition
      return (
        <FilterConditionComponent
          key={rule.id}
          condition={rule}
          fields={fields}
          onUpdateCondition={(conditionId, updates) => {
            // Find and update the condition in the current group
            const updatedRules = group.rules.map((r) =>
              r.id === conditionId ? { ...r, ...updates } : r,
            );
            onUpdateGroup(group.id, { rules: updatedRules });
          }}
          onRemove={onRemove}
        />
      );
    } else {
      // It's a FilterGroup
      return (
        <FilterGroupComponent
          key={rule.id}
          group={rule}
          fields={fields}
          onUpdateGroup={onUpdateGroup}
          onAddCondition={onAddCondition}
          onAddGroup={onAddGroup}
          onRemove={onRemove}
          depth={depth + 1}
        />
      );
    }
  };

  return (
    <Paper
      withBorder={!isRoot}
      p={isRoot ? 0 : "sm"}
      bg={getBackgroundColor(depth)}
    >
      <Stack gap="sm">
        {/* Group header with combinator and remove button */}
        <Flex justify="space-between" align="center">
          {/* Show combinator only if there are multiple rules or if it's not the root */}
          {(group.rules.length > 1 || !isRoot) && (
            <SegmentedControl
              value={group.combinator}
              onChange={handleCombinatorChange}
              data={[
                { label: "AND", value: "AND" },
                { label: "OR", value: "OR" },
              ]}
              size="xs"
            />
          )}

          {/* Remove button for non-root groups */}
          {!isRoot && (
            <ActionIcon
              variant="subtle"
              color="red"
              size="sm"
              onClick={() => onRemove(group.id)}
              aria-label="Remove group"
            >
              <IconX size={16} />
            </ActionIcon>
          )}
        </Flex>

        {/* Rules with scoping line */}
        <Box
          style={{
            borderLeft:
              !isRoot && group.rules.length > 0
                ? "2px solid var(--mantine-color-gray-3)"
                : "none",
            paddingLeft: !isRoot && group.rules.length > 0 ? 16 : 0,
          }}
        >
          <Stack gap="xs">
            {group.rules.map((rule) => (
              <Box key={rule.id}>{renderRule(rule)}</Box>
            ))}
          </Stack>
        </Box>

        {/* Show message when no rules */}
        {group.rules.length === 0 && (
          <Box
            style={{
              padding: "20px",
              textAlign: "center",
              color: "var(--mantine-color-gray-6)",
              fontStyle: "italic",
            }}
          >
            No conditions added yet. Click "Condition" or "Group" to add
            filters.
          </Box>
        )}

        {/* Add buttons at the bottom */}
        <Group gap="xs">
          <Button
            size="xs"
            variant="light"
            leftSection={<IconPlus size={14} />}
            onClick={() => onAddCondition(group.id)}
          >
            Condition
          </Button>

          <Button
            size="xs"
            variant="light"
            leftSection={<IconPlus size={14} />}
            onClick={() => onAddGroup(group.id)}
          >
            Group
          </Button>
        </Group>
      </Stack>
    </Paper>
  );
}
