import { render as sharedRender, type Options } from "@lab/test-utils";
import { StatsigProvider } from "@statsig/react-bindings";
import type { PropsWithChildren, ReactNode } from "react";
import { statsigClient } from "./statsig";

export function render(
  ui: ReactNode,
  options?: Options,
): ReturnType<typeof sharedRender> {
  return sharedRender(ui, { ...options, wrapper: StatsigWrapper });
}

function StatsigWrapper({ children }: PropsWithChildren) {
  return (
    <StatsigProvider client={statsigClient as never}>
      {children}
    </StatsigProvider>
  );
}
