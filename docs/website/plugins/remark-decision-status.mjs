import path from "node:path";

const BADGE_BY_STATUS = {
  Accepted: "success",
  Proposed: "info",
  Superseded: "secondary",
  Deprecated: "secondary",
  Rejected: "danger",
};

const longDate = new Intl.DateTimeFormat("en-GB", {
  day: "numeric",
  month: "long",
  year: "numeric",
  timeZone: "UTC",
});

const escapeHtml = (text) =>
  String(text).replace(
    /[&<>"]/g,
    (character) => `&#${character.charCodeAt(0)};`,
  );

// Docusaurus doesn't render front matter, so show each decision record's
// `status` and `date` under its title.
export default function remarkDecisionStatus({ decisionsDir }) {
  return (tree, file) => {
    const { status, date } = file.data.frontMatter ?? {};
    if (!status || path.relative(decisionsDir, file.path).startsWith("..")) {
      return;
    }

    const parts = [
      `<span class="badge badge--${BADGE_BY_STATUS[status] ?? "secondary"}">${escapeHtml(status)}</span>`,
    ];
    const decided = date ? new Date(date) : undefined;
    if (decided && !Number.isNaN(decided.getTime())) {
      parts.push(
        `<time datetime="${decided.toISOString().slice(0, 10)}">${longDate.format(decided)}</time>`,
      );
    }

    const title = tree.children.findIndex(
      (node) => node.type === "heading" && node.depth === 1,
    );
    tree.children.splice(title + 1, 0, {
      type: "html",
      value: `<p>${parts.join(" ")}</p>`,
    });
  };
}
