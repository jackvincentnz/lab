import { MantineProvider } from "@mantine/core";
import { Shell } from "@lab/bubbles";
import Tasks from "../tasks";

export default function App() {
  return (
    <MantineProvider>
      <Shell title="Tasks">
        <Tasks />
      </Shell>
    </MantineProvider>
  );
}
