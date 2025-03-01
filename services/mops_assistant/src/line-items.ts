import { GraphQLClient } from "graphql-request";
import {
  AddLineItemDocument,
  AddLineItemMutation,
  AllLineItemsDocument,
  AllLineItemsQuery,
} from "./__generated__/graphql.js";

const endpoint = process.env["GRAPHQL_HOST"] || "http://localhost:8080/graphql";
const client = new GraphQLClient(endpoint);

export async function addLineItem(lineItem: { name: string }) {
  return client.request<AddLineItemMutation>(AddLineItemDocument, {
    input: {
      name: lineItem.name,
    },
  });
}

export async function allLineItems() {
  return client.request<AllLineItemsQuery>(AllLineItemsDocument);
}
