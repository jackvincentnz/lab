import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { MantineProvider } from "@mantine/core";
import "@mantine/core/styles.css";
import { App } from "./App";
import { createSlateClient, createQueryClient } from "./api";
import { SlateProvider } from "./SlateProvider";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <MantineProvider>
      <SlateProvider
        client={createSlateClient("/api")}
        queryClient={createQueryClient()}
      >
        <App />
      </SlateProvider>
    </MantineProvider>
  </StrictMode>,
);
