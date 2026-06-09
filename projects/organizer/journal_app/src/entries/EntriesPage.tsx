import { useQuery } from "@apollo/client/react";
import { Timeline, type TimelineItemProps } from "./Timeline";

import { GetEntriesDocument } from "../__generated__/graphql";

export function EntriesPage() {
  const { data } = useQuery(GetEntriesDocument, { pollInterval: 500 });

  const items =
    data?.allEntries.map(
      (entry): TimelineItemProps => ({
        title: entry.message,
        message: entry.message,
        when: entry.createdAt,
      }),
    ) || [];

  return <Timeline items={items} />;
}

export default EntriesPage;
