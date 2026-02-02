import { Table, Text } from "@mantine/core";
import ReactMarkdown, { Components } from "react-markdown";
import remarkGfm from "remark-gfm";

interface MarkdownContentProps {
  content: string;
}

const MARKDOWN_COMPONENTS: Components = {
  table: ({ children, ...props }) => (
    <Table my="md" striped highlightOnHover withTableBorder {...props}>
      {children}
    </Table>
  ),
  thead: ({ children, ...props }) => (
    <Table.Thead {...props}>{children}</Table.Thead>
  ),
  tbody: ({ children, ...props }) => (
    <Table.Tbody {...props}>{children}</Table.Tbody>
  ),
  tr: ({ children, ...props }) => <Table.Tr {...props}>{children}</Table.Tr>,
  th: ({ children, ...props }) => <Table.Th {...props}>{children}</Table.Th>,
  td: ({ children, ...props }) => <Table.Td {...props}>{children}</Table.Td>,
  p: ({ children }) => <Text my="md">{children}</Text>,
};

export function MarkdownContent({ content }: MarkdownContentProps) {
  return (
    <ReactMarkdown remarkPlugins={[remarkGfm]} components={MARKDOWN_COMPONENTS}>
      {content}
    </ReactMarkdown>
  );
}
