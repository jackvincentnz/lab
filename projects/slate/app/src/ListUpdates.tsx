import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  type PropsWithChildren,
} from "react";
import { focusManager, useQueryClient } from "@tanstack/react-query";
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

  const changed = useCallback(() => {
    if (holds.current > 0) {
      held.current = true;
    } else {
      reload();
    }
  }, [reload]);

  useSubscription(
    trpc.listChanged.subscriptionOptions(undefined, {
      // A change made while disconnected, such as during a service restart, sends no event.
      onStarted: changed,
      onData: changed,
    }),
  );

  // Without a change, the list still changes at midnight, when the items done the day before drop off.
  useEffect(
    () =>
      focusManager.subscribe((focused) => {
        if (focused) {
          changed();
        }
      }),
    [changed],
  );

  const hold = useCallback<Hold>(() => {
    holds.current += 1;
    // A reload already under way could still take away the item being edited.
    if (holds.current === 1 && queryClient.isFetching() > 0) {
      held.current = true;
      void queryClient.cancelQueries();
    }
    return () => {
      holds.current -= 1;
      if (holds.current === 0 && held.current) {
        held.current = false;
        reload();
      }
    };
  }, [queryClient, reload]);

  return <HoldContext value={hold}>{children}</HoldContext>;
}

// Rows moving under a drag, or the open item vanishing from under an edit, would lose what the user is doing.
export function useHoldListUpdates(holding: boolean) {
  const hold = useContext(HoldContext);

  useEffect(() => (holding ? hold() : undefined), [hold, holding]);
}
