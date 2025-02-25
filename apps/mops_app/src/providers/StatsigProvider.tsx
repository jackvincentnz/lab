import { PropsWithChildren } from "react";
import {
  StatsigProvider as NativeProvider,
  useClientAsyncInit,
} from "@statsig/react-bindings";
import { StatsigAutoCapturePlugin } from "@statsig/web-analytics";
import { StatsigSessionReplayPlugin } from "@statsig/session-replay";

import { ClientConfigurationDocument } from "../__generated__/graphql";
import { useQuery } from "@apollo/client";

export function StatsigProvider({ children }: PropsWithChildren) {
  const { data } = useQuery(ClientConfigurationDocument);

  if (!data?.clientConfiguration.statsigKey) return children;

  return (
    <EnabledProvider statsigKey={data.clientConfiguration.statsigKey}>
      {children}
    </EnabledProvider>
  );
}

function EnabledProvider({
  statsigKey,
  children,
}: PropsWithChildren<{ statsigKey: string }>) {
  const { client } = useClientAsyncInit(
    statsigKey,
    { userID: "a-user" },
    {
      plugins: [
        new StatsigAutoCapturePlugin(),
        new StatsigSessionReplayPlugin(),
      ],
    },
  );

  return <NativeProvider client={client}>{children}</NativeProvider>;
}
