import { Fragment, useRef, type FormEvent } from "react";
import {
  Anchor,
  Box,
  Button,
  Group,
  SegmentedControl,
  Stack,
  Text,
  TextInput,
  Title,
} from "@mantine/core";
import type {
  Item,
  NewSource,
  ItemSource as Source,
  UpdateItemInput,
} from "../api";
import { type Draft, useItemDraft, type SaveDraft } from "./useItemDraft";

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

interface FocusTarget {
  id: string | undefined;
}

export function ItemSources({
  item,
  disabled,
  saving,
  onSourcesChange,
  onSourcesChangeCancel,
}: ItemSourcesProps) {
  const editingId = useRef<string>(undefined);
  const focusTarget = useRef<FocusTarget>(undefined);
  const { draft, start, change, submit, cancel } = useItemDraft(
    item,
    (shown) => sourceDraftOf(findSource(shown, editingId.current)),
    (editedFrom, value: SourceDraft, { onSaved, onChanged }) => {
      const saved = sourceOf(value);
      onSourcesChange(
        editedFrom,
        sourcesChange(findSource(editedFrom, editingId.current), saved),
        {
          onSaved: (savedItem) => {
            const savedSource =
              saved === null
                ? undefined
                : savedItem.sources.find(
                    (source) => sourceKey(source) === sourceKey(saved),
                  );
            editingId.current = savedSource?.id;
            focusTarget.current = { id: savedSource?.id };
            onSaved(savedItem);
          },
          onChanged,
        },
      );
    },
  );
  const editing =
    draft === undefined ? undefined : findSource(item, editingId.current);

  function submitSource(event: FormEvent) {
    event.preventDefault();
    submit();
  }

  function cancelSource() {
    if (!saving) {
      focusTarget.current = { id: editing?.id };
      onSourcesChangeCancel();
      cancel();
    }
  }

  function startSource(source: Source | undefined) {
    editingId.current = source?.id;
    start();
  }

  // Closing the editor removes the control that had focus, which leaves focus on the page.
  function focusAfterEdit(id: string | undefined) {
    return (button: HTMLButtonElement | null) => {
      if (
        button !== null &&
        focusTarget.current !== undefined &&
        focusTarget.current.id === id
      ) {
        focusTarget.current = undefined;
        if (document.activeElement === document.body) {
          button.focus();
        }
      }
    };
  }

  function editor() {
    return draft === undefined ? null : (
      <SourceEditor
        item={item}
        editing={editing}
        adding={editingId.current === undefined}
        draft={draft}
        disabled={disabled}
        change={change}
        onSubmit={submitSource}
        onCancel={cancelSource}
        onRemove={() => submit(noSource)}
      />
    );
  }

  return (
    <Stack gap="xs">
      <Title order={3} size="xs" c="dimmed" tt="uppercase">
        Sources
      </Title>
      {/* One keyed list, so that the editor moves with its source instead of remounting and taking focus. */}
      {[
        ...item.sources.map((source) => {
          if (source.id === editing?.id) {
            return <Fragment key="editor">{editor()}</Fragment>;
          }
          return (
            <Group
              key={source.id}
              justify="space-between"
              wrap="nowrap"
              align="flex-start"
            >
              <Box flex={1} miw={0}>
                <SourceView source={source} />
              </Box>
              {draft === undefined && (
                <Button
                  ref={focusAfterEdit(source.id)}
                  variant="subtle"
                  size="compact-xs"
                  style={{ flexShrink: 0 }}
                  aria-label={`Edit source ${source.label}`}
                  onClick={() => startSource(source)}
                >
                  Edit
                </Button>
              )}
            </Group>
          );
        }),
        draft === undefined ? (
          <Button
            key="add"
            ref={focusAfterEdit(undefined)}
            variant="default"
            size="xs"
            style={{ alignSelf: "flex-start" }}
            onClick={() => startSource(undefined)}
          >
            Add a source
          </Button>
        ) : (
          editing === undefined && <Fragment key="editor">{editor()}</Fragment>
        ),
      ]}
    </Stack>
  );
}

interface SourceEditorProps {
  item: Item;
  editing: Source | undefined;
  adding: boolean;
  draft: Draft<SourceDraft>;
  disabled: boolean;
  change(value: SourceDraft): void;
  onSubmit(event: FormEvent): void;
  onCancel(): void;
  onRemove(): void;
}

function SourceEditor({
  item,
  editing,
  adding,
  draft,
  disabled,
  change,
  onSubmit,
  onCancel,
  onRemove,
}: SourceEditorProps) {
  const targetError =
    sourceTargetError(draft.value) ?? repeatError(item, editing, draft.value);

  return (
    <form
      onSubmit={onSubmit}
      onKeyDown={(event) => {
        if (event.key === "Escape") {
          event.preventDefault();
          onCancel();
        }
      }}
    >
      <Stack gap="xs">
        {draft.refusedFrom !== undefined && (
          <Text size="xs" c="dimmed" component="div">
            {item.updatedAt === draft.refusedFrom.updatedAt || adding ? (
              "Changed elsewhere."
            ) : editing === undefined ? (
              "Changed elsewhere. This source is no longer on the item, so Save adds it."
            ) : (
              <>
                Changed elsewhere. Current source:{" "}
                <SourceView source={editing} />
              </>
            )}
          </Text>
        )}
        <TextInput
          label="Label"
          autoFocus
          value={draft.value.label}
          onChange={(event) =>
            change({ ...draft.value, label: event.currentTarget.value })
          }
        />
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
            onClick={onCancel}
          >
            Cancel
          </Button>
          {editing !== undefined && (
            <Button
              variant="subtle"
              color="red"
              size="xs"
              ml="auto"
              data-disabled={disabled || undefined}
              aria-disabled={disabled}
              onClick={onRemove}
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
  saved: NewSource | null,
): SourcesChange {
  if (editing === undefined) {
    return { addSources: saved === null ? [] : [saved] };
  }
  return saved === null
    ? { removeSources: [editing.id] }
    : { replaceSource: { id: editing.id, to: saved } };
}

function sourceKey(source: NewSource): string {
  return source.kind === "link" ? source.url : source.text;
}

function findSource(item: Item, id: string | undefined): Source | undefined {
  return id === undefined
    ? undefined
    : item.sources.find((source) => source.id === id);
}

function repeatError(
  item: Item,
  editing: Source | undefined,
  draft: SourceDraft,
): string | undefined {
  const target = draft.target.trim();
  const editingKey = editing === undefined ? undefined : sourceKey(editing);
  return item.sources.some(
    (source) =>
      sourceKey(source) !== editingKey && sourceKey(source) === target,
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

function sourceOf({ kind, target, label }: SourceDraft): NewSource | null {
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
