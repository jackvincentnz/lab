import { useQuery } from "@apollo/client";
import { Timeline, TimelineItemProps } from "./Timeline";

import { GetEntriesDocument } from "../__generated__/graphql"; // FIXME: types not working :/

export function EntriesPage() {
  const { data } = useQuery(GetEntriesDocument, { pollInterval: 500 });

  const items = data?.allEntries.map(mapEntry) || [];

  return <Timeline items={items} />;
}

function mapEntry(entry: any): TimelineItemProps {
  return {
    title: entry.message,
    message: entry.message,
    when: entry.createdAt,
  };
}

export default EntriesPage;
