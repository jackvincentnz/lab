import { vi } from "vitest";

const gates: Record<string, boolean> = {};

type LogEvent = (
  eventName: string,
  value?: string,
  metadata?: Record<string, string>,
) => void;

const logEventMock = vi.fn<LogEvent>();
const checkGateMock = vi.fn((gate: string) => Boolean(gates[gate]));

export const logEvent: LogEvent = (...args) => {
  logEventMock(...args);
};

export interface StatsigClient {
  logEvent: LogEvent;
  checkGate: (gate: string) => boolean;
}

export const statsigClient: StatsigClient = {
  logEvent,
  checkGate: (gate: string) => checkGateMock(gate),
};

export function setStatsigGate(gate: string, enabled: boolean) {
  gates[gate] = enabled;
}

export function resetStatsigMock() {
  logEventMock.mockClear();
  checkGateMock.mockClear();

  for (const gate of Object.keys(gates)) {
    delete gates[gate];
  }
}
