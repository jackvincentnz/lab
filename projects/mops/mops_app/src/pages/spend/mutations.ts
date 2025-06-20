import { useMutation } from "@apollo/client";
import { NewLineItem } from "./components/spend-table";
import {
  AddLineItemDocument,
  AddLineItemInput,
  SpendPageQueryDocument,
  DeleteAllLineItemsDocument,
} from "../../__generated__/graphql";

export function useAddLineItemMutation(budgetId: string) {
  const [addLineItemMutation] = useMutation(AddLineItemDocument, {
    refetchQueries: [SpendPageQueryDocument],
  });

  return function addLineItem(lineItem: NewLineItem) {
    const addLineItemInput: AddLineItemInput = {
      budgetId,
      name: lineItem.name,
    };

    if (lineItem.fields.length > 0) {
      addLineItemInput.categorizations = lineItem.fields.map((field) => ({
        categoryId: field.id,
        categoryValueId: field.value,
      }));
    }

    addLineItemMutation({ variables: { input: addLineItemInput } });
  };
}

export function useDeleteAllLineItemsMutation() {
  const [deleteAllLineItemsMutation] = useMutation(DeleteAllLineItemsDocument, {
    refetchQueries: [SpendPageQueryDocument],
  });

  return () => {
    deleteAllLineItemsMutation();
  };
}
