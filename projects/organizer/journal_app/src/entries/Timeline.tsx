import { Timeline as MTimeline, Text } from "@mantine/core";

export interface TimelineProps {
  items: TimelineItemProps[];
}

export interface TimelineItemProps {
  title: string;
  message: string;
  when: string;
}

export function Timeline({ items }: TimelineProps) {
  const timelineItems = items.map((item) => (
    <MTimeline.Item key={item.title} title={item.title}>
      <Text color="dimmed" size="sm">
        {item.message}
      </Text>
      <Text size="xs" mt={4}>
        {item.when}
      </Text>
    </MTimeline.Item>
  ));

  return <MTimeline>{timelineItems}</MTimeline>;
}
