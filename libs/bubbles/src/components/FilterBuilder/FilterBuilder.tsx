import { useState, useCallback } from "react";
import {
  FilterBuilderProps,
  FilterState,
  FilterGroup,
  FilterCondition,
} from "./types";
import { FilterGroupComponent } from "./FilterGroupComponent";

// Utility function to generate unique IDs
const generateId = () =>
  `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

// Default initial state
const createDefaultState = (): FilterState => ({
  id: "root",
  combinator: "AND",
  rules: [],
});

export function FilterBuilder({
  fields,
  initialState,
  onChange,
}: FilterBuilderProps) {
  const [state, setState] = useState<FilterState>(
    initialState || createDefaultState(),
  );

  // Immutable update helper - recursively finds and updates a group
  const updateGroupInState = useCallback(
    (
      currentState: FilterState,
      groupId: string,
      updates: Partial<FilterGroup>,
    ): FilterState => {
      if (currentState.id === groupId) {
        const newState = { ...currentState, ...updates };
        onChange?.(newState);
        return newState;
      }

      const updatedRules = currentState.rules.map((rule) => {
        if ("combinator" in rule) {
          // It's a FilterGroup, recurse
          return updateGroupInState(rule, groupId, updates);
        }
        return rule; // It's a FilterCondition, return as-is
      });

      const newState = { ...currentState, rules: updatedRules };
      if (currentState.id === "root") {
        onChange?.(newState);
      }
      return newState;
    },
    [onChange],
  );

  // Remove item (condition or group) from state
  const removeFromState = useCallback(
    (currentState: FilterState, itemId: string): FilterState => {
      // Filter out the item with the matching ID
      const updatedRules = currentState.rules
        .filter((rule) => rule.id !== itemId)
        .map((rule) => {
          if ("combinator" in rule) {
            // It's a FilterGroup, recurse to remove from nested groups
            return removeFromState(rule, itemId);
          }
          return rule; // It's a FilterCondition, return as-is
        });

      const newState = { ...currentState, rules: updatedRules };
      if (currentState.id === "root") {
        onChange?.(newState);
      }
      return newState;
    },
    [onChange],
  );

  // Add condition to a specific group
  const addConditionToGroup = useCallback(
    (currentState: FilterState, groupId: string): FilterState => {
      if (currentState.id === groupId) {
        const newCondition: FilterCondition = {
          id: generateId(),
          field: fields.length > 0 ? fields[0].name : "",
          operator: fields.length > 0 ? fields[0].operators[0] || "eq" : "eq",
          value:
            fields.length > 0
              ? fields[0].type === "number"
                ? 0
                : fields[0].type === "date"
                  ? null
                  : ""
              : "",
        };

        const newState = {
          ...currentState,
          rules: [...currentState.rules, newCondition],
        };
        onChange?.(newState);
        return newState;
      }

      const updatedRules = currentState.rules.map((rule) => {
        if ("combinator" in rule) {
          // It's a FilterGroup, recurse
          return addConditionToGroup(rule, groupId);
        }
        return rule; // It's a FilterCondition, return as-is
      });

      const newState = { ...currentState, rules: updatedRules };
      if (currentState.id === "root") {
        onChange?.(newState);
      }
      return newState;
    },
    [fields, onChange],
  );

  // Add group to a specific group
  const addGroupToGroup = useCallback(
    (currentState: FilterState, parentGroupId: string): FilterState => {
      if (currentState.id === parentGroupId) {
        const newGroup: FilterGroup = {
          id: generateId(),
          combinator: "AND",
          rules: [],
        };

        const newState = {
          ...currentState,
          rules: [...currentState.rules, newGroup],
        };
        onChange?.(newState);
        return newState;
      }

      const updatedRules = currentState.rules.map((rule) => {
        if ("combinator" in rule) {
          // It's a FilterGroup, recurse
          return addGroupToGroup(rule, parentGroupId);
        }
        return rule; // It's a FilterCondition, return as-is
      });

      const newState = { ...currentState, rules: updatedRules };
      if (currentState.id === "root") {
        onChange?.(newState);
      }
      return newState;
    },
    [onChange],
  );

  // Handler functions
  const handleUpdateGroup = useCallback(
    (groupId: string, updates: Partial<FilterGroup>) => {
      setState((currentState) =>
        updateGroupInState(currentState, groupId, updates),
      );
    },
    [updateGroupInState],
  );

  const handleAddCondition = useCallback(
    (groupId: string) => {
      setState((currentState) => addConditionToGroup(currentState, groupId));
    },
    [addConditionToGroup],
  );

  const handleAddGroup = useCallback(
    (groupId: string) => {
      setState((currentState) => addGroupToGroup(currentState, groupId));
    },
    [addGroupToGroup],
  );

  const handleRemove = useCallback(
    (itemId: string) => {
      setState((currentState) => removeFromState(currentState, itemId));
    },
    [removeFromState],
  );

  return (
    <FilterGroupComponent
      group={state}
      fields={fields}
      onUpdateGroup={handleUpdateGroup}
      onAddCondition={handleAddCondition}
      onAddGroup={handleAddGroup}
      onRemove={handleRemove}
      depth={0}
    />
  );
}
