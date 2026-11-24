import type { PropsWithChildren } from "react";
import { QueryClientProvider, type QueryClient } from "@tanstack/react-query";
import { TRPCProvider, type SlateClient } from "./api";

interface SlateProviderProps {
  client: SlateClient;
  queryClient: QueryClient;
}

export function SlateProvider({
  client,
  queryClient,
  children,
}: PropsWithChildren<SlateProviderProps>) {
  return (
    <QueryClientProvider client={queryClient}>
      <TRPCProvider trpcClient={client} queryClient={queryClient}>
        {children}
      </TRPCProvider>
    </QueryClientProvider>
  );
}
