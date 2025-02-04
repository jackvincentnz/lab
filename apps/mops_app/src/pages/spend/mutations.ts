import { useMutation } from "@apollo/client";
import { NewLineItem } from "./components/spend-table";
import {
  AddLineItemDocument,
  AllLineItemsDocument,
} from "../../__generated__/graphql";

export function useAddLineItemMutation() {
  const [addLineItemMutation] = useMutation(AddLineItemDocument, {
    refetchQueries: [AllLineItemsDocument],
  });

  return function addLineItem(lineItem: NewLineItem) {
    addLineItemMutation({ variables: { input: { name: lineItem.name } } });
  };
}
