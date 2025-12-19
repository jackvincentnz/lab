import { LineItem, LineItemErrors, NewLineItem } from "./types";
import { validateLineItem } from "./validation";
import { MRT_TableOptions } from "mantine-react-table";
import { useStatsigClient } from "@statsig/react-bindings";

export function useAddLineItem(
  onValidationErrors: (errors: LineItemErrors) => void,
  onAddLineItem?: (lineItem: NewLineItem) => void,
): MRT_TableOptions<LineItem>["onCreatingRowSave"] {
  const { client } = useStatsigClient();

  return (props: {
    values: Record<string, string>;
    exitCreatingMode: () => void;
  }) => {
    const lineItem = mapToLineItem(props.values);

    const newValidationErrors = validateLineItem(lineItem);
    if (Object.values(newValidationErrors).some((error) => error)) {
      onValidationErrors(newValidationErrors);
      return;
    }
    onValidationErrors({});

    onAddLineItem && onAddLineItem(lineItem);

    client.logEvent("add_line_item", lineItem.name, {
      name: lineItem.name,
    });

    props.exitCreatingMode();
  };
}

function mapToLineItem(values: Record<string, string>): NewLineItem {
  const fields = Object.entries(values)
    .filter(([columnId, columnValue]) => columnId !== "name" && !!columnValue)
    .map(([columnId, value]) => ({ id: columnId, value }));

  return {
    name: values["name"],
    fields,
  };
}
