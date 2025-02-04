export interface LineItem {
  id: string;
  name: string;
}

export type NewLineItem = Omit<LineItem, "id">;

export type LineItemErrors = Partial<Record<keyof LineItem, string>>;
