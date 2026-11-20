import { Alert, Button, Loader, Text } from "@mantine/core";
import type { ReactNode } from "react";

interface FailureProps {
  retry: () => void;
}

export function Failure({ retry }: FailureProps) {
  return (
    <Alert color="red" title="Couldn’t load this view" role="alert">
      Check that the WIDE service is running.
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

interface EmptyProps {
  children: ReactNode;
}

export function Empty({ children }: EmptyProps) {
  return <div className="empty">{children}</div>;
}
