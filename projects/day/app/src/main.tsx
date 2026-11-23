import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { MantineProvider } from "@mantine/core";
import "@mantine/core/styles.css";
import { App } from "./App";
import { createDayClient, createQueryClient } from "./api";
import { DayProvider } from "./DayProvider";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <MantineProvider>
      <DayProvider
        client={createDayClient("/api")}
        queryClient={createQueryClient()}
      >
        <App />
      </DayProvider>
    </MantineProvider>
  </StrictMode>,
);
