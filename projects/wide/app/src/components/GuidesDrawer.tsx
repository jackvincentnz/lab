import { Drawer, Text } from "@mantine/core";
import problemGuide from "../../../skills/wide-refine/references/problem-template.md?raw";
import solutionGuide from "../../../skills/wide-refine/references/solution-template.md?raw";
import { Prose } from "./Prose";

export function GuidesDrawer({
  opened,
  onClose,
}: {
  opened: boolean;
  onClose: () => void;
}) {
  return (
    <Drawer
      opened={opened}
      onClose={onClose}
      title="Shared refinement guides"
      position="right"
      size="xl"
    >
      <Text c="dimmed" mb="lg">
        The agent uses these same guides. Missing information is a prompt for a
        useful conversation, not a reason to invent an answer.
      </Text>
      <Prose>{problemGuide}</Prose>
      <hr />
      <Prose>{solutionGuide}</Prose>
    </Drawer>
  );
}
