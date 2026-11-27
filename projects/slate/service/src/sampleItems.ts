import { type ItemSource, type ItemStatus } from "./item.js";
import { type ItemStore } from "./store.js";

export interface SampleItem {
  name: string;
  description?: string;
  sources?: ItemSource[];
  status: ItemStatus;
  deleted?: boolean;
}

/** Made up, because this repository is public. */
export const sampleItems: SampleItem[] = [
  {
    name: "Write the launch announcement",
    description:
      "Draft for the team blog.\n\n- Lead with what changes for readers\n- Link the [release notes](https://example.com/notes)",
    status: "in_progress",
  },
  {
    name: "Review the onboarding checklist",
    sources: [
      {
        kind: "reference",
        text: "notes/TODO.md#Onboarding",
        label: "TODO list",
      },
    ],
    status: "todo",
  },
  {
    name: "Plan next quarter's goals",
    description: "Pick at most three, and say why each matters.",
    sources: [
      {
        kind: "link",
        url: "https://example.com/chat/quarter-planning",
        label: "Claude Desktop session",
      },
      {
        kind: "link",
        url: "https://example.com/docs/last-quarter-review",
        label: "Last quarter's review",
      },
      {
        kind: "reference",
        text: "notes/daily/2026-11-27.md#Goals",
        label: "Daily note",
      },
    ],
    status: "todo",
  },
  { name: "Fix the flaky export test", status: "in_progress" },
  { name: "Renew the team's domain name", status: "done" },
  { name: "Answer the survey about desk bookings", status: "done" },
  { name: "Redesign the team wiki", status: "deferred" },
  { name: "Migrate the old chat archive", status: "wont_do" },
  {
    name: "Order new monitors",
    description: "Handed to Sam in IT.",
    status: "delegated",
  },
  { name: "Book a room for the design review", status: "todo" },
  {
    name: "Try the old idea for weekly digests",
    status: "todo",
    deleted: true,
  },
];

/** Moves each item after the previous one, so that their order does not depend on where `add` puts an item. */
export function addSampleItems(store: ItemStore): void {
  const deleted: string[] = [];
  let previous = store.list().at(-1);
  for (const {
    name,
    description = "",
    sources = [],
    status,
    ...sample
  } of sampleItems) {
    let added = store.add(name, description, sources);
    if (previous) {
      added = store.move({
        id: added.id,
        neighbourId: previous.id,
        position: "after",
      })!;
    }
    if (status !== added.status) {
      added = store.update({
        id: added.id,
        status,
        updatedAt: added.updatedAt,
      })!;
    }
    if (sample.deleted) {
      deleted.push(added.id);
    }
    previous = added;
  }

  for (const id of deleted) {
    store.delete(id);
  }
}
