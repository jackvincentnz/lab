import type { PropsWithChildren } from "react";
import { QueryClientProvider, type QueryClient } from "@tanstack/react-query";
import { TRPCProvider, type DayClient } from "./api";

interface DayProviderProps {
  client: DayClient;
  queryClient: QueryClient;
}

export function DayProvider({
  client,
  queryClient,
  children,
}: PropsWithChildren<DayProviderProps>) {
  return (
    <QueryClientProvider client={queryClient}>
      <TRPCProvider trpcClient={client} queryClient={queryClient}>
        {children}
      </TRPCProvider>
    </QueryClientProvider>
  );
}
