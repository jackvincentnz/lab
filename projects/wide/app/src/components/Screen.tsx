import { ActionIcon } from "@mantine/core";
import type { ReactNode } from "react";
import { icon } from "./icons";

export function Screen({
  crumbs,
  refresh,
  syncedAt,
  loading,
  children,
}: {
  crumbs: ReactNode[];
  refresh: () => void;
  syncedAt: Date | null;
  loading: boolean;
  children: ReactNode;
}) {
  return (
    <>
      <header className="topbar">
        <nav aria-label="Breadcrumb" className="crumbs">
          {crumbs.map((crumb, i) => (
            <span key={i} className="crumb">
              {i > 0 && icon.chevron}
              {crumb}
            </span>
          ))}
        </nav>
        <div className="sync">
          <span>
            {syncedAt
              ? `Synced ${syncedAt.toLocaleTimeString(undefined, {
                  timeStyle: "short",
                })}`
              : "Syncing…"}
          </span>
          <ActionIcon
            variant="default"
            aria-label="Refresh"
            title="Refresh"
            loading={loading}
            onClick={refresh}
          >
            {icon.refresh}
          </ActionIcon>
        </div>
      </header>
      <div className="page">{children}</div>
    </>
  );
}
