import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { MantineProvider } from "@mantine/core";
import "@fontsource-variable/inter/index.css";
import "@mantine/core/styles.css";
import { App } from "./App";
import { createSlateClient, createQueryClient } from "./api";
import { SlateProvider } from "./SlateProvider";
import { theme } from "./theme";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <MantineProvider theme={theme}>
      <SlateProvider
        client={createSlateClient("/api")}
        queryClient={createQueryClient()}
      >
        <App />
      </SlateProvider>
    </MantineProvider>
  </StrictMode>,
);
