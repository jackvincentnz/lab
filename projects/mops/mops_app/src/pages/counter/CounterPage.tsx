import { useSubscription } from "@apollo/client";
import { CounterPageSubscriptionDocument } from "../../__generated__/graphql";

export function CounterPage() {
  const { data } = useSubscription(CounterPageSubscriptionDocument);

  return <p>{data?.counter}</p>;
}
