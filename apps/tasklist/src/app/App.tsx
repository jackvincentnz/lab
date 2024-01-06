// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-expect-error
import { AppShell } from "@lab/bubbles"; // FIXME: type safety for the library
import Tasks from "../tasks";

export default function App() {
  return (
    <AppShell title="Tasks">
      <Tasks />
    </AppShell>
  );
}
