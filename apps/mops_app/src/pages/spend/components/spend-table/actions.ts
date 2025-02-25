import { LineItem, LineItemErrors, NewLineItem } from "./types";
import { validateLineItem } from "./validation";
import { MRT_TableOptions } from "mantine-react-table";
import { useStatsigClient } from "@statsig/react-bindings";

export function useAddLineItem(
  onValidationErrors: (errors: LineItemErrors) => void,
  onAddLineItem?: (lineItem: NewLineItem) => void,
): MRT_TableOptions<LineItem>["onCreatingRowSave"] {
  const { client } = useStatsigClient();

  return (props: { values: LineItem; exitCreatingMode: () => void }) => {
    const newValidationErrors = validateLineItem(props.values);
    if (Object.values(newValidationErrors).some((error) => error)) {
      onValidationErrors(newValidationErrors);
      return;
    }
    onValidationErrors({});

    onAddLineItem && onAddLineItem(props.values);

    client.logEvent("add_line_item", props.values.name, {
      name: props.values.name,
    });

    props.exitCreatingMode();
  };
}
