import "@mantine/core/styles.css";
import { MantineProvider } from "@mantine/core";
import { theme } from "./theme";
import { Shell } from "./components/shell";

export default function App() {
  return (
    <MantineProvider theme={theme}>
      <Shell />
    </MantineProvider>
  );
}
