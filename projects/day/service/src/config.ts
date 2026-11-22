import { z } from "zod";

const environment = z.object({
  DAY_DATA_DIR: z.string().min(1),
  DAY_PORT: z.coerce.number().int().min(1).max(65535).default(5197),
});

export interface Config {
  dataDir: string;
  port: number;
}

export function readConfig(env: NodeJS.ProcessEnv): Config {
  const { DAY_DATA_DIR, DAY_PORT } = environment.parse(env);
  return { dataDir: DAY_DATA_DIR, port: DAY_PORT };
}
