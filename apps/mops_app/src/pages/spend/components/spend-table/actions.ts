import { LineItem, LineItemErrors, NewLineItem } from "./types";
import { validateLineItem } from "./validation";
import { MRT_TableOptions } from "mantine-react-table";

export function useAddLineItem(
  onValidationErrors: (errors: LineItemErrors) => void,
  onAddLineItem?: (lineItem: NewLineItem) => void,
): MRT_TableOptions<LineItem>["onCreatingRowSave"] {
  return (props: { values: LineItem; exitCreatingMode: () => void }) => {
    const newValidationErrors = validateLineItem(props.values);
    if (Object.values(newValidationErrors).some((error) => error)) {
      onValidationErrors(newValidationErrors);
      return;
    }
    onValidationErrors({});

    onAddLineItem && onAddLineItem(props.values);

    props.exitCreatingMode();
  };
}
