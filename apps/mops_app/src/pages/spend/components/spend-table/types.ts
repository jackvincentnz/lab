export interface LineItem {
  id: string;
  name: string;
  fields: Field[];
}

export interface Field {
  id: string;
  value: string;
}

export interface Column {
  id: string;
  header: string;
  options: Option[];
  accessor: (lineItem: LineItem) => string | undefined;
}

export interface Option {
  value: string;
  label: string;
}

export type NewLineItem = Omit<LineItem, "id">;

export type LineItemErrors = Partial<Record<keyof LineItem, string>>;
