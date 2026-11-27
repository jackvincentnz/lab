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
import type { Item, ItemSource as Source, UpdateItemInput } from "../api";
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
  key: string | undefined;
}

export function ItemSources({
  item,
  disabled,
  saving,
  onSourcesChange,
  onSourcesChangeCancel,
}: ItemSourcesProps) {
  // Found by url or text rather than position, which shifts when a source is removed elsewhere.
  const editingKey = useRef<string>(undefined);
  // The list reloads after a save, so until then the source still has the url or text it had.
  const savedFromKey = useRef<string>(undefined);
  const findEditing = (shown: Item) =>
    findSource(shown, editingKey.current) ??
    findSource(shown, savedFromKey.current);
  const focusTarget = useRef<FocusTarget>(undefined);
  const { draft, start, change, submit, cancel } = useItemDraft(
    item,
    (shown) => sourceDraftOf(findEditing(shown)),
    (editedFrom, value: SourceDraft, { onSaved, onChanged }) => {
      const saved = sourceOf(value);
      const savedKey = saved === null ? undefined : sourceKey(saved);
      focusTarget.current = { key: savedKey };
      onSourcesChange(
        editedFrom,
        sourcesChange(findEditing(editedFrom), saved),
        {
          onSaved: (savedItem) => {
            savedFromKey.current = editingKey.current;
            editingKey.current = savedKey;
            onSaved(savedItem);
          },
          onChanged,
        },
      );
    },
  );
  const editing = draft === undefined ? undefined : findEditing(item);
  if (findSource(item, editingKey.current) !== undefined) {
    savedFromKey.current = undefined;
  }

  function submitSource(event: FormEvent) {
    event.preventDefault();
    submit();
  }

  function cancelSource() {
    if (!saving) {
      focusTarget.current = {
        key: editing === undefined ? undefined : sourceKey(editing),
      };
      onSourcesChangeCancel();
      cancel();
    }
  }

  function startSource(source: Source | undefined) {
    editingKey.current = source === undefined ? undefined : sourceKey(source);
    savedFromKey.current = undefined;
    start();
  }

  // Closing the editor removes the control that had focus, which leaves focus on the page.
  function focusAfterEdit(key: string | undefined) {
    return (button: HTMLButtonElement | null) => {
      if (
        button !== null &&
        focusTarget.current !== undefined &&
        focusTarget.current.key === key
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
        adding={editingKey.current === undefined}
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
          const key = sourceKey(source);
          if (editing !== undefined && sourceKey(editing) === key) {
            return <Fragment key="editor">{editor()}</Fragment>;
          }
          return (
            <Group
              key={`source ${key}`}
              justify="space-between"
              wrap="nowrap"
              align="flex-start"
            >
              <Box flex={1} miw={0}>
                <SourceView source={source} />
              </Box>
              {draft === undefined && (
                <Button
                  ref={focusAfterEdit(key)}
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

function findSource(item: Item, key: string | undefined): Source | undefined {
  return key === undefined
    ? undefined
    : item.sources.find((source) => sourceKey(source) === key);
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
