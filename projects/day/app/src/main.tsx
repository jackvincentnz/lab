import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { MantineProvider } from "@mantine/core";
import "@mantine/core/styles.css";
import { App } from "./App";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    {/* Both render inline style elements, which the service's policy blocks; styles.css already
        holds the default theme's variables. */}
    <MantineProvider withCssVariables={false} withGlobalClasses={false}>
      <App />
    </MantineProvider>
  </StrictMode>,
);
