import { Alert, Button, Loader, Text } from "@mantine/core";
import type { ReactNode } from "react";

export function Failure({ retry }: { retry: () => void }) {
  return (
    <Alert color="red" title="Couldn’t load this view" role="alert">
      Check that the WIDE service is running. For a detail view, also check the
      record link.
      <div>
        <Button mt="sm" variant="light" color="red" onClick={retry}>
          Try again
        </Button>
      </div>
    </Alert>
  );
}

export function Loading() {
  return (
    <div className="loading" role="status">
      <Loader size="sm" />
      <Text>Loading…</Text>
    </div>
  );
}

export function Empty({ children }: { children: ReactNode }) {
  return <div className="empty">{children}</div>;
}
