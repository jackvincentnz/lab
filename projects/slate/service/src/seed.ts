import { readConfig, readProdDataDir } from "./config.js";
import { resetToSampleItems } from "./dataFolder.js";

try {
  const { dataDir } = readConfig(process.env);
  resetToSampleItems(dataDir, readProdDataDir(process.env));
  console.log(`Replaced the data in ${dataDir} with sample items.`);
} catch (error) {
  console.error("Slate cannot seed the data folder:", (error as Error).message);
  process.exitCode = 1;
}
