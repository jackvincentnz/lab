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
import type { Item, ItemSource as Source, UpdateItemInput } from "../api";
import { useItemDraft, type SaveDraft } from "./useItemDraft";

export type SourcesChange = Pick<
  UpdateItemInput,
  "addSources" | "removeSources" | "replaceSource"
>;

interface ItemSourcesProps {
  item: Item;
  disabled: boolean;
  saving: boolean;
  onSourcesChange: SaveDraft<SourcesChange>;
  onSourcesChangeCancel(): void;
}

type SourceKind = Source["kind"];

interface SourceDraft {
  kind: SourceKind;
  target: string;
  label: string;
}

const noSource: SourceDraft = { kind: "link", target: "", label: "" };

const maxReferenceLength = 1000;

export function ItemSources(props: ItemSourcesProps) {
  const { item } = props;
  return (
    <Stack gap="xs">
      <Title order={3} size="xs" c="dimmed" tt="uppercase">
        Sources
      </Title>
      {/* Keyed by position, so that a source edited in place keeps its editor and focus. */}
      {[...item.sources, undefined].map((_source, index) => (
        <SourceRow key={index} index={index} {...props} />
      ))}
    </Stack>
  );
}

interface SourceRowProps extends ItemSourcesProps {
  /** The source's position, or the number of sources for the row that adds one. */
  index: number;
}

function SourceRow({
  item,
  index,
  disabled,
  saving,
  onSourcesChange,
  onSourcesChangeCancel,
}: SourceRowProps) {
  const { draft, start, change, submit, cancel } = useItemDraft(
    item,
    (shown) => sourceDraftOf(shown.sources[index]),
    (editedFrom, value: SourceDraft, callbacks) =>
      onSourcesChange(
        editedFrom,
        sourcesChange(editedFrom.sources[index], sourceOf(value)),
        callbacks,
      ),
  );
  const returnFocus = useRef(false);
  const source = item.sources[index];

  function submitSource(event: FormEvent) {
    event.preventDefault();
    submit();
  }

  function cancelSource() {
    if (!saving) {
      onSourcesChangeCancel();
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
    return source === undefined ? (
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
      <Group justify="space-between" wrap="nowrap" align="flex-start">
        <SourceView source={source} />
        <Button
          ref={focusAfterEdit}
          variant="subtle"
          size="compact-xs"
          aria-label={`Edit source ${source.label}`}
          onClick={startSource}
        >
          Edit
        </Button>
      </Group>
    );
  }

  const targetError =
    sourceTargetError(draft.value) ?? repeatError(item, index, draft.value);

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
        {draft.refusedFrom !== undefined && (
          <Text size="xs" c="dimmed" component="div">
            {item.updatedAt === draft.refusedFrom.updatedAt ? (
              "Changed elsewhere."
            ) : source === undefined ? (
              "Changed elsewhere. This source is no longer on the item, so Save adds it."
            ) : (
              <>
                Changed elsewhere. Current source:{" "}
                <SourceView source={source} />
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
          {source !== undefined && (
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

function sourcesChange(
  editing: Source | undefined,
  saved: Source | null,
): SourcesChange {
  if (editing === undefined) {
    return { addSources: saved === null ? [] : [saved] };
  }
  return saved === null
    ? { removeSources: [sourceKey(editing)] }
    : { replaceSource: { from: sourceKey(editing), to: saved } };
}

function sourceKey(source: Source): string {
  return source.kind === "link" ? source.url : source.text;
}

function repeatError(
  item: Item,
  index: number,
  draft: SourceDraft,
): string | undefined {
  const target = draft.target.trim();
  return item.sources.some(
    (source, other) => other !== index && sourceKey(source) === target,
  )
    ? "The item already has this source"
    : undefined;
}

function sourceDraftOf(source: Source | undefined): SourceDraft {
  if (source === undefined) {
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
