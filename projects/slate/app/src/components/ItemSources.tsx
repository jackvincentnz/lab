import { type RefObject, useRef } from "react";
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

export function ItemSources(props: ItemSourcesProps) {
  const addButton = useRef<HTMLButtonElement>(null);
  return (
    <Stack gap="xs">
      <Title order={3} size="xs" c="dimmed" tt="uppercase">
        Sources
      </Title>
      {props.item.sources.map((source) => (
        <SourceRow
          key={source.id}
          source={source}
          onRemoved={() => addButton.current?.focus()}
          {...props}
        />
      ))}
      <AddSource addButton={addButton} {...props} />
    </Stack>
  );
}

interface SourceRowProps extends ItemSourcesProps {
  source: Source;
  onRemoved(): void;
}

function SourceRow({
  item,
  source,
  disabled,
  saving,
  onSourcesChange,
  onSourcesChangeCancel,
  onRemoved,
}: SourceRowProps) {
  const returnFocus = useReturnFocus();
  const { draft, start, change, submit, cancel } = useItemDraft(
    item,
    (shown) => sourceDraftOf(shown.sources.find(({ id }) => id === source.id)),
    (editedFrom, value: SourceDraft, callbacks) => {
      const saved = sourceOf(value);
      if (saved !== null) {
        onSourcesChange(
          editedFrom,
          { replaceSource: { id: source.id, to: saved } },
          callbacks,
        );
        return;
      }
      onSourcesChange(
        editedFrom,
        { removeSources: [source.id] },
        {
          ...callbacks,
          onSaved: (savedItem) => {
            callbacks.onSaved(savedItem);
            onRemoved();
          },
        },
      );
    },
  );

  if (draft === undefined) {
    return (
      <Group justify="space-between" wrap="nowrap" align="flex-start">
        <Box flex={1} miw={0}>
          <SourceView source={source} />
        </Box>
        <Button
          ref={returnFocus.ref}
          variant="subtle"
          size="compact-xs"
          style={{ flexShrink: 0 }}
          aria-label={`Edit source ${source.label}`}
          onClick={() => {
            returnFocus.expect();
            start();
          }}
        >
          Edit
        </Button>
      </Group>
    );
  }

  return (
    <SourceEditor
      item={item}
      editingId={source.id}
      current={source}
      draft={draft}
      disabled={disabled}
      change={change}
      onSubmit={() => submit()}
      onCancel={() => {
        if (!saving) {
          onSourcesChangeCancel();
          cancel();
        }
      }}
      onRemove={() => submit(noSource)}
    />
  );
}

interface AddSourceProps extends ItemSourcesProps {
  addButton: RefObject<HTMLButtonElement | null>;
}

function AddSource({
  item,
  disabled,
  saving,
  onSourcesChange,
  onSourcesChangeCancel,
  addButton,
}: AddSourceProps) {
  const returnFocus = useReturnFocus();
  const { draft, start, change, submit, cancel } = useItemDraft(
    item,
    () => noSource,
    (editedFrom, value: SourceDraft, callbacks) => {
      const saved = sourceOf(value);
      if (saved !== null) {
        onSourcesChange(editedFrom, { addSources: [saved] }, callbacks);
      }
    },
  );

  if (draft === undefined) {
    return (
      <Button
        ref={(button) => {
          addButton.current = button;
          returnFocus.ref(button);
        }}
        variant="default"
        size="xs"
        style={{ alignSelf: "flex-start" }}
        onClick={() => {
          returnFocus.expect();
          start();
        }}
      >
        Add a source
      </Button>
    );
  }

  return (
    <SourceEditor
      item={item}
      editingId={undefined}
      current={undefined}
      draft={draft}
      disabled={disabled}
      change={change}
      onSubmit={() => submit()}
      onCancel={() => {
        if (!saving) {
          onSourcesChangeCancel();
          cancel();
        }
      }}
    />
  );
}

/** Closing an editor removes the control that had focus, which leaves focus on the page. */
function useReturnFocus() {
  const returning = useRef(false);
  return {
    expect() {
      returning.current = true;
    },
    ref(button: HTMLButtonElement | null) {
      if (button !== null && returning.current) {
        returning.current = false;
        if (document.activeElement === document.body) {
          button.focus();
        }
      }
    },
  };
}

interface SourceEditorProps {
  item: Item;
  editingId: string | undefined;
  current: Source | undefined;
  draft: Draft<SourceDraft>;
  disabled: boolean;
  change(value: SourceDraft): void;
  onSubmit(): void;
  onCancel(): void;
  onRemove?(): void;
}

function SourceEditor({
  item,
  editingId,
  current,
  draft,
  disabled,
  change,
  onSubmit,
  onCancel,
  onRemove,
}: SourceEditorProps) {
  const targetError =
    sourceTargetError(draft.value) ?? repeatError(item, editingId, draft.value);

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit();
      }}
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
            {item.updatedAt === draft.refusedFrom.updatedAt ||
            current === undefined ? (
              "Changed elsewhere."
            ) : (
              <>
                Changed elsewhere. Current source:{" "}
                <SourceView source={current} />
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
          {onRemove !== undefined && (
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

function sourceKey(source: NewSource): string {
  return source.kind === "link" ? source.url : source.text;
}

function repeatError(
  item: Item,
  editingId: string | undefined,
  draft: SourceDraft,
): string | undefined {
  const target = draft.target.trim();
  return item.sources.some(
    (source) => source.id !== editingId && sourceKey(source) === target,
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
