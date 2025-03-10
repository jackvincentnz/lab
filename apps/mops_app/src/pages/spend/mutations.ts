import { useMutation } from "@apollo/client";
import { NewLineItem } from "./components/spend-table";
import {
  AddLineItemDocument,
  AddLineItemInput,
  AllLineItemsDocument,
  DeleteAllLineItemsDocument,
} from "../../__generated__/graphql";

export function useAddLineItemMutation() {
  const [addLineItemMutation] = useMutation(AddLineItemDocument, {
    refetchQueries: [AllLineItemsDocument],
  });

  return function addLineItem(lineItem: NewLineItem) {
    const addLineItemInput: AddLineItemInput = { name: lineItem.name };

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
    refetchQueries: [AllLineItemsDocument],
  });

  return () => {
    deleteAllLineItemsMutation();
  };
}
