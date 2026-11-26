import { createRequire } from "node:module";
import { launch } from "./launch.js";

// Under Node, `electron` exports the path to the Electron binary.
const electron: string = createRequire(import.meta.url)("electron");

launch(electron, process.argv.slice(2));
