import { attachedDocs } from "../sidebars.mjs";

const STATUS_ORDER = [
  "Accepted",
  "Proposed",
  "Superseded",
  "Deprecated",
  "Rejected",
];

// Folder names such as `docs` or `src/main/java` say where a file sits in the
// repository, not what it is about, so they are left out of the sidebar.
function flatten(items) {
  return items.flatMap((item) => {
    if (item.type !== "category") {
      return [item];
    }
    const children = flatten(item.items);
    if (!item.link && (item.label === "docs" || children.length === 1)) {
      return children;
    }
    return [{ ...item, items: children }];
  });
}

// Groups decision records by the `status` in their front matter, so that a
// record moves group when its status changes, newest `date` first.
function groupByStatus(items, docs) {
  const statusById = new Map(
    docs.map((doc) => [doc.id, doc.frontMatter.status]),
  );
  const timeById = new Map(
    docs.map((doc) => [doc.id, new Date(doc.frontMatter.date ?? 0).getTime()]),
  );
  const newestFirst = (a, b) =>
    (timeById.get(b.id) || 0) - (timeById.get(a.id) || 0);
  const groups = new Map();
  const ungrouped = [];

  for (const item of items) {
    const status = item.type === "doc" ? statusById.get(item.id) : undefined;
    if (status) {
      groups.set(status, [...(groups.get(status) ?? []), item]);
    } else {
      ungrouped.push(item);
    }
  }

  const rank = (status) => {
    const index = STATUS_ORDER.indexOf(status);
    return index === -1 ? STATUS_ORDER.length : index;
  };
  const statuses = [...groups.keys()].sort(
    (a, b) => rank(a) - rank(b) || a.localeCompare(b),
  );

  return [
    ...ungrouped,
    ...statuses.map((status) => ({
      type: "category",
      label: status,
      collapsed: false,
      items: groups.get(status).sort(newestFirst),
    })),
  ];
}

function attach(items) {
  return items.map((item) => {
    if (item.type !== "category") {
      return item;
    }
    const attached = attachedDocs[item.link?.id] ?? [];
    return {
      ...item,
      items: [
        ...attach(item.items),
        ...attached.map((id) => ({ type: "doc", id })),
      ],
    };
  });
}

export default async function sidebarItems({
  defaultSidebarItemsGenerator,
  ...args
}) {
  const items = attach(flatten(await defaultSidebarItemsGenerator(args)));
  return args.item.dirName === "docs/adr"
    ? groupByStatus(items, args.docs)
    : items;
}
