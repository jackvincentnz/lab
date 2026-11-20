import { useQuery } from "@apollo/client/react";
import { ActionIcon, TextInput } from "@mantine/core";
import { Link, useSearchParams } from "react-router-dom";
import { icon } from "../components/icons";
import { LinkedTo } from "../components/LinkedTo";
import { Screen } from "../components/Screen";
import { Empty, Failure, Loading } from "../components/Status";
import { When } from "../components/When";
import { useSynced } from "../hooks/useSynced";
import { labels, listQueries, pageSize, type Kind } from "../lib/kinds";

interface CollectionPageProps {
  kind: Kind;
}

export function CollectionPage({ kind }: CollectionPageProps) {
  const [params, setParams] = useSearchParams();
  const pageValue = Number(params.get("page") || 0);
  const page =
    Number.isSafeInteger(pageValue) && pageValue >= 0 && pageValue <= 2147483647
      ? pageValue
      : 0;
  const query = params.get("query") || "";
  const { data, loading, error, refetch } = useQuery(listQueries[kind], {
    variables: {
      page,
      limit: pageSize,
      ...(kind === "signals" ? { query } : {}),
    },
    fetchPolicy: "network-only",
  });
  const syncedAt = useSynced(data);
  const entries = data?.[kind] || [];
  // A full page may be followed by another; a short first page is the whole collection.
  const morePages = page > 0 || entries.length === pageSize;

  const update = (change: (next: URLSearchParams) => void) =>
    setParams((previous) => {
      const next = new URLSearchParams(previous);
      change(next);
      return next;
    });
  const go = (nextPage: number) =>
    update((next) => {
      if (nextPage > 0) next.set("page", String(nextPage));
      else next.delete("page");
    });
  const search = (value: string) =>
    update((next) => {
      next.delete("page");
      if (value) next.set("query", value);
      else next.delete("query");
    });

  const stamp = kind === "signals" ? "Captured" : "Updated";
  const emptyMessage =
    page > 0
      ? "No more records on this page. Return to the previous page."
      : query
        ? "No signals match this search."
        : `No ${labels[
            kind
          ].toLowerCase()} yet. Use your connected agent to capture or refine an opportunity.`;

  return (
    <Screen
      crumbs={[labels[kind]]}
      refresh={() => void refetch()}
      syncedAt={syncedAt}
      loading={loading}
    >
      <div className="page-head">
        <h1>{labels[kind]}</h1>
      </div>
      {kind === "signals" && (
        <form
          className="search"
          role="search"
          onSubmit={(event) => {
            event.preventDefault();
            search(
              String(new FormData(event.currentTarget).get("query") || ""),
            );
          }}
        >
          <TextInput
            aria-label="Search signals"
            placeholder="Filter by title, saved content, or source, then press Enter"
            leftSection={icon.search}
            key={query}
            name="query"
            defaultValue={query}
          />
        </form>
      )}
      {loading ? (
        <Loading />
      ) : error ? (
        <Failure retry={() => void refetch()} />
      ) : (
        <>
          {entries.length === 0 ? (
            <Empty>{emptyMessage}</Empty>
          ) : (
            <div className={`table ${kind}`}>
              <div className="thead" aria-hidden="true">
                <span>Title</span>
                <span>Linked to</span>
                <span className="right">{stamp}</span>
              </div>
              {entries.map((entry) => (
                <Link
                  className="row"
                  key={entry.id}
                  to={`/${kind}/${entry.id}`}
                >
                  <span className="row-title">
                    <span className="row-title-text">{entry.title}</span>
                    {kind === "signals" && (
                      <span
                        className={entry.source ? "row-sub" : "row-sub dim"}
                      >
                        {entry.source || "No source"}
                      </span>
                    )}
                  </span>
                  <span>
                    <LinkedTo kind={kind} entry={entry} />
                  </span>
                  <span className="muted right">
                    <When value={entry.capturedAt || entry.updatedAt} />
                  </span>
                </Link>
              ))}
            </div>
          )}
          {morePages && (
            <div className="pager">
              <span className="muted">
                Newest {kind === "signals" ? "captures" : "updates"} first ·{" "}
                {pageSize} per page
                {page > 0 ? ` · page ${page + 1}` : ""}
              </span>
              <div className="pager-buttons">
                <ActionIcon
                  variant="default"
                  aria-label="Previous"
                  disabled={page === 0}
                  onClick={() => go(page - 1)}
                >
                  {icon.previous}
                </ActionIcon>
                <ActionIcon
                  variant="default"
                  aria-label="Next"
                  disabled={entries.length < pageSize}
                  onClick={() => go(page + 1)}
                >
                  {icon.chevron}
                </ActionIcon>
              </div>
            </div>
          )}
        </>
      )}
    </Screen>
  );
}
