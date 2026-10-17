import { useQuery } from "@apollo/client/react";
import { Prose } from "../components/Prose";
import { Screen } from "../components/Screen";
import { Empty, Failure, Loading } from "../components/Status";
import { When } from "../components/When";
import { useSynced } from "../hooks/useSynced";
import { StrategyDocument } from "../__generated__/graphql";

export function StrategyPage() {
  const { data, loading, error, refetch } = useQuery(StrategyDocument, {
    fetchPolicy: "network-only",
  });
  const syncedAt = useSynced(data);
  return (
    <Screen
      crumbs={["Strategy"]}
      refresh={() => void refetch()}
      syncedAt={syncedAt}
      loading={loading}
    >
      <div className="page-head">
        <div>
          <h1>Strategy</h1>
          {data?.strategy.updatedAt && (
            <p className="page-sub">
              <When verb="Updated" value={data.strategy.updatedAt} /> · the
              objectives and tradeoffs that inform refinement
            </p>
          )}
        </div>
      </div>
      {loading ? (
        <Loading />
      ) : error ? (
        <Failure retry={() => void refetch()} />
      ) : !data?.strategy.content ? (
        <Empty>
          No strategy has been set. Discuss and save it with your connected
          agent.
        </Empty>
      ) : (
        <article className="record reading">
          <Prose>{data.strategy.content}</Prose>
        </article>
      )}
    </Screen>
  );
}
