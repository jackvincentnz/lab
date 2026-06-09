import { useSubscription } from "@apollo/client/react";
import { CounterPageSubscriptionDocument } from "../../__generated__/graphql";

export function CounterPage() {
  const { data } = useSubscription(CounterPageSubscriptionDocument);

  return <p>{String(data?.counter ?? "")}</p>;
}
