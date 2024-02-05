import { MantineProvider } from "@mantine/core";
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-expect-error
import { Shell } from "@lab/bubbles"; // FIXME: type safety for the library
import Tasks from "../tasks";

import "@lab/bubbles/style.css";

export default function App() {
  return (
    <MantineProvider>
      <Shell title="Tasks">
        <Tasks />
      </Shell>
    </MantineProvider>
  );
}
