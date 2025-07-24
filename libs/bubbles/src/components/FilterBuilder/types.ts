// A single filter condition row
export interface FilterCondition {
  id: string; // Unique ID for React keys and state updates
  field: string;
  operator: string;
  value: any;
}

// A group of conditions/groups
export interface FilterGroup {
  id: string; // Unique ID
  combinator: "AND" | "OR";
  rules: (FilterCondition | FilterGroup)[]; // The array can contain either type
}

// The root state
export type FilterState = FilterGroup;

// Generic field configuration
export interface FieldDefinition {
  name: string; // e.g., 'firstName'
  label: string; // e.g., 'First Name'
  type: "string" | "number" | "date" | "select";
  operators: string[]; // e.g., ['eq', 'neq', 'contains']
  options?: { label: string; value: string }[]; // For 'select' type
}

// Props for the main FilterBuilder component
export interface FilterBuilderProps {
  fields: FieldDefinition[];
  initialState?: FilterState;
  onChange?: (state: FilterState) => void;
}

// Props for FilterGroupComponent
export interface FilterGroupProps {
  group: FilterGroup;
  fields: FieldDefinition[];
  onUpdateGroup: (groupId: string, updates: Partial<FilterGroup>) => void;
  onAddCondition: (groupId: string) => void;
  onAddGroup: (groupId: string) => void;
  onRemove: (id: string) => void;
  depth?: number;
}

// Props for FilterConditionComponent
export interface FilterConditionProps {
  condition: FilterCondition;
  fields: FieldDefinition[];
  onUpdateCondition: (
    conditionId: string,
    updates: Partial<FilterCondition>,
  ) => void;
  onRemove: (id: string) => void;
}
