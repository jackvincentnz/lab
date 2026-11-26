import { createRequire } from "node:module";
import { launch } from "./launch.js";

// Required from Node rather than Electron, the `electron` package is the path to its binary.
const electron: string = createRequire(import.meta.url)("electron");

launch(electron, process.argv.slice(2));
