import { MantineProvider } from "@mantine/core";
import { Shell } from "@lab/bubbles";
import Tasks from "./tasks";

import "@mantine/core/styles.css";

export default function App() {
  return (
    <MantineProvider>
      <Shell title="Tasks">
        <Tasks />
      </Shell>
    </MantineProvider>
  );
}
