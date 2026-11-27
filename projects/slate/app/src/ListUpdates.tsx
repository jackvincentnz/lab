import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  type PropsWithChildren,
} from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useSubscription } from "@trpc/tanstack-react-query";
import { useTRPC } from "./api";

type Hold = () => () => void;

const HoldContext = createContext<Hold>(() => () => undefined);

export function ListUpdates({ children }: PropsWithChildren) {
  const trpc = useTRPC();
  const queryClient = useQueryClient();
  const holds = useRef(0);
  const held = useRef(false);

  const reload = useCallback(
    () => void queryClient.invalidateQueries(),
    [queryClient],
  );

  // A change missed while disconnected, such as during sleep or a service restart, has no event, so connecting reloads too.
  function changed() {
    if (holds.current > 0) {
      held.current = true;
    } else {
      reload();
    }
  }

  useSubscription(
    trpc.listChanged.subscriptionOptions(undefined, {
      onStarted: changed,
      onData: changed,
    }),
  );

  const hold = useCallback<Hold>(() => {
    holds.current += 1;
    return () => {
      holds.current -= 1;
      if (holds.current === 0 && held.current) {
        held.current = false;
        reload();
      }
    };
  }, [reload]);

  return <HoldContext value={hold}>{children}</HoldContext>;
}

// Rows moving under a drag, or the open item vanishing from under an edit, would lose what the user is doing.
export function useHoldListUpdates(holding: boolean) {
  const hold = useContext(HoldContext);

  useEffect(() => (holding ? hold() : undefined), [hold, holding]);
}
