import { useRef, type FormEvent } from "react";
import {
  Anchor,
  Button,
  Group,
  SegmentedControl,
  Stack,
  Text,
  TextInput,
  Title,
} from "@mantine/core";
import type { Item, ItemSource as Source } from "../api";
import { useItemDraft, type SaveDraft } from "./useItemDraft";

interface ItemSourceProps {
  item: Item;
  disabled: boolean;
  saving: boolean;
  onSourceChange: SaveDraft<Source | null>;
  onSourceChangeCancel(): void;
}

type SourceKind = Source["kind"];

interface SourceDraft {
  kind: SourceKind;
  target: string;
  label: string;
}

const noSource: SourceDraft = { kind: "link", target: "", label: "" };

const maxReferenceLength = 1000;

export function ItemSource({
  item,
  disabled,
  saving,
  onSourceChange,
  onSourceChangeCancel,
}: ItemSourceProps) {
  const { draft, start, change, submit, cancel } = useItemDraft(
    item,
    sourceDraftOf,
    (editedFrom, value: SourceDraft, callbacks) =>
      onSourceChange(editedFrom, sourceOf(value), callbacks),
  );
  const returnFocus = useRef(false);

  function submitSource(event: FormEvent) {
    event.preventDefault();
    submit();
  }

  function cancelSource() {
    if (!saving) {
      onSourceChangeCancel();
      cancel();
    }
  }

  function startSource() {
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
            Source
          </Title>
          {item.source !== null && (
            <Button
              ref={focusAfterEdit}
              variant="subtle"
              size="compact-xs"
              aria-label="Edit source"
              onClick={startSource}
            >
              Edit
            </Button>
          )}
        </Group>
        {item.source === null ? (
          <Button
            ref={focusAfterEdit}
            variant="default"
            size="xs"
            style={{ alignSelf: "flex-start" }}
            onClick={startSource}
          >
            Add a source
          </Button>
        ) : (
          <SourceView source={item.source} />
        )}
      </Stack>
    );
  }

  const targetError = sourceTargetError(draft.value);

  return (
    <form
      onSubmit={submitSource}
      onKeyDown={(event) => {
        if (event.key === "Escape") {
          event.preventDefault();
          cancelSource();
        }
      }}
    >
      <Stack gap="xs">
        <Title order={3} size="xs" c="dimmed" tt="uppercase">
          Source
        </Title>
        {draft.refusedFrom !== undefined && (
          <Text size="xs" c="dimmed" component="div">
            {item.updatedAt === draft.refusedFrom.updatedAt ? (
              "Changed elsewhere."
            ) : item.source === null ? (
              "Changed elsewhere. The item now has no source."
            ) : (
              <>
                Changed elsewhere. Current source:{" "}
                <SourceView source={item.source} />
              </>
            )}
          </Text>
        )}
        <SegmentedControl
          aria-label="Kind of source"
          size="xs"
          data={[
            { value: "link", label: "Link" },
            { value: "reference", label: "Reference" },
          ]}
          value={draft.value.kind}
          onChange={(kind) =>
            change({ ...draft.value, kind: kind as SourceKind })
          }
          style={{ alignSelf: "flex-start" }}
        />
        <TextInput
          key={draft.value.kind}
          label={draft.value.kind === "link" ? "Link" : "Reference"}
          placeholder={
            draft.value.kind === "link"
              ? "https://"
              : "A file path, claude:// link or anything else"
          }
          value={draft.value.target}
          error={draft.value.target.trim() !== "" && targetError}
          onChange={(event) =>
            change({ ...draft.value, target: event.currentTarget.value })
          }
          autoFocus
        />
        <TextInput
          label="Label"
          value={draft.value.label}
          onChange={(event) =>
            change({ ...draft.value, label: event.currentTarget.value })
          }
        />
        <Group gap="xs">
          <Button
            type="submit"
            size="xs"
            disabled={
              targetError !== undefined || draft.value.label.trim() === ""
            }
            data-disabled={disabled || undefined}
            aria-disabled={disabled}
          >
            Save
          </Button>
          <Button
            variant="default"
            size="xs"
            data-disabled={disabled || undefined}
            aria-disabled={disabled}
            onClick={cancelSource}
          >
            Cancel
          </Button>
          {item.source !== null && (
            <Button
              variant="subtle"
              color="red"
              size="xs"
              ml="auto"
              data-disabled={disabled || undefined}
              aria-disabled={disabled}
              onClick={() => submit(noSource)}
            >
              Remove source
            </Button>
          )}
        </Group>
      </Stack>
    </form>
  );
}

function sourceDraftOf({ source }: Item): SourceDraft {
  if (source === null) {
    return noSource;
  }
  return source.kind === "link"
    ? { kind: "link", target: source.url, label: source.label }
    : { kind: "reference", target: source.text, label: source.label };
}

function sourceOf({ kind, target, label }: SourceDraft): Source | null {
  if (target === "" && label === "") {
    return null;
  }
  return kind === "link"
    ? { kind, url: target.trim(), label: label.trim() }
    : { kind, text: target.trim(), label: label.trim() };
}

function sourceTargetError({ kind, target }: SourceDraft): string | undefined {
  const trimmed = target.trim();
  if (kind === "link") {
    return isSourceLink(trimmed)
      ? undefined
      : "Give a link that starts with https://";
  }
  if (trimmed === "") {
    return "Give a reference that is not blank";
  }
  return trimmed.length > maxReferenceLength
    ? `Give a reference of at most ${maxReferenceLength} characters`
    : undefined;
}

/** The desktop app opens only https links in the browser. */
function isSourceLink(url: string): boolean {
  return URL.parse(url)?.protocol === "https:";
}

/** The desktop app opens claude links in Claude Desktop, and no other reference. */
function isClaudeLink(text: string): boolean {
  return URL.parse(text)?.protocol === "claude:";
}

interface SourceViewProps {
  source: Source;
}

function SourceView({ source }: SourceViewProps) {
  if (source.kind === "link") {
    return <ExternalLink href={source.url}>{source.label}</ExternalLink>;
  }
  return (
    <Stack gap={2} component="span">
      {isClaudeLink(source.text) ? (
        <ExternalLink href={source.text}>{source.label}</ExternalLink>
      ) : (
        <Text size="sm" component="span">
          {source.label}
        </Text>
      )}
      <Text
        size="xs"
        c="dimmed"
        ff="monospace"
        component="span"
        style={{ overflowWrap: "anywhere", userSelect: "all" }}
      >
        {source.text}
      </Text>
    </Stack>
  );
}

interface ExternalLinkProps {
  href: string;
  children: string;
}

function ExternalLink({ href, children }: ExternalLinkProps) {
  return (
    <Anchor
      href={href}
      target="_blank"
      rel="noreferrer"
      size="sm"
      style={{ overflowWrap: "anywhere" }}
    >
      {children}
    </Anchor>
  );
}
