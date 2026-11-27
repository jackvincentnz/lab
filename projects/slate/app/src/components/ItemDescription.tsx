import { useRef, type FormEvent } from "react";
import {
  Button,
  Group,
  Stack,
  Text,
  Textarea,
  Title,
  Typography,
} from "@mantine/core";
import Markdown from "react-markdown";
import remarkGfm from "remark-gfm";
import type { Item } from "../api";
import { useItemDraft, type SaveDraft } from "./useItemDraft";

interface ItemDescriptionProps {
  item: Item;
  saving: boolean;
  pending: boolean;
  onDescribe: SaveDraft;
  onDescribeCancel(): void;
}

export function ItemDescription({
  item,
  saving,
  pending,
  onDescribe,
  onDescribeCancel,
}: ItemDescriptionProps) {
  const { draft, start, change, submit, cancel } = useItemDraft(
    item,
    "description",
    onDescribe,
  );
  const returnFocus = useRef(false);

  function submitDescription(event: FormEvent) {
    event.preventDefault();
    if (!pending) {
      submit();
    }
  }

  function cancelDescription() {
    if (!pending) {
      onDescribeCancel();
      cancel();
    }
  }

  function startDescription() {
    returnFocus.current = true;
    start();
  }

  // Closing the editor removes the control that had focus, which leaves focus on the page.
  function focusAfterEdit(button: HTMLButtonElement | null) {
    if (button !== null && returnFocus.current) {
      returnFocus.current = false;
      if (document.activeElement === document.body) {
        button.focus();
      }
    }
  }

  if (draft === undefined) {
    return (
      <Stack gap="xs">
        <Group justify="space-between">
          <Title order={3} size="xs" c="dimmed" tt="uppercase">
            Description
          </Title>
          {item.description !== "" && (
            <Button
              ref={focusAfterEdit}
              variant="subtle"
              size="compact-xs"
              aria-label="Edit description"
              onClick={startDescription}
            >
              Edit
            </Button>
          )}
        </Group>
        {item.description === "" ? (
          <Button
            ref={focusAfterEdit}
            variant="default"
            size="xs"
            style={{ alignSelf: "flex-start" }}
            onClick={startDescription}
          >
            Add a description
          </Button>
        ) : (
          <Description markdown={item.description} />
        )}
      </Stack>
    );
  }

  return (
    <form
      onSubmit={submitDescription}
      onKeyDown={(event) => {
        if (event.key === "Escape") {
          event.preventDefault();
          cancelDescription();
        }
      }}
    >
      <Stack gap="xs">
        <Title order={3} size="xs" c="dimmed" tt="uppercase">
          Description
        </Title>
        {draft.refusedFrom !== undefined && (
          <Text size="xs" c="dimmed" component="div">
            {item.updatedAt === draft.refusedFrom.updatedAt ? (
              "Changed elsewhere."
            ) : item.description === "" ? (
              "Changed elsewhere. The description is now empty."
            ) : (
              <>
                Changed elsewhere. Current description:
                <Description markdown={item.description} />
              </>
            )}
          </Text>
        )}
        <Textarea
          label="Markdown"
          value={draft.value}
          onChange={(event) => change(event.currentTarget.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter" && event.metaKey) {
              event.preventDefault();
              event.currentTarget.form?.requestSubmit();
            }
          }}
          ref={focusAtEnd}
          autosize
          minRows={6}
          styles={{
            input: { fontFamily: "var(--mantine-font-family-monospace)" },
          }}
        />
        <Group gap="xs">
          <Button
            type="submit"
            size="xs"
            data-disabled={saving || undefined}
            aria-disabled={saving}
          >
            Save
          </Button>
          <Button
            variant="default"
            size="xs"
            data-disabled={saving || undefined}
            aria-disabled={saving}
            onClick={cancelDescription}
          >
            Cancel
          </Button>
        </Group>
      </Stack>
    </form>
  );
}

function focusAtEnd(textarea: HTMLTextAreaElement | null) {
  if (textarea !== null) {
    textarea.focus();
    textarea.setSelectionRange(textarea.value.length, textarea.value.length);
  }
}

interface DescriptionProps {
  markdown: string;
}

function Description({ markdown }: DescriptionProps) {
  return (
    <Typography fz="sm" style={{ overflowWrap: "anywhere" }}>
      <Markdown
        remarkPlugins={[remarkGfm]}
        skipHtml
        components={{
          a: ({ href, children }) => (
            <a href={href} target="_blank" rel="noreferrer">
              {children}
            </a>
          ),
        }}
      >
        {markdown}
      </Markdown>
    </Typography>
  );
}
