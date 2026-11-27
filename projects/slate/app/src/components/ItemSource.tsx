import { useRef, type FormEvent } from "react";
import {
  Anchor,
  Button,
  Group,
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

interface SourceDraft {
  url: string;
  label: string;
}

const noSource: SourceDraft = { url: "", label: "" };

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
          <SourceLink source={item.source} />
        )}
      </Stack>
    );
  }

  const urlIsLink = isSourceLink(draft.value.url.trim());

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
                <SourceLink source={item.source} />
              </>
            )}
          </Text>
        )}
        <TextInput
          label="Link"
          placeholder="https://"
          value={draft.value.url}
          error={
            draft.value.url.trim() !== "" &&
            !urlIsLink &&
            "Give a link that starts with https://"
          }
          onChange={(event) =>
            change({ ...draft.value, url: event.currentTarget.value })
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
            disabled={!urlIsLink || draft.value.label.trim() === ""}
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

function sourceDraftOf(item: Item): SourceDraft {
  return item.source === null
    ? noSource
    : { url: item.source.url, label: item.source.label };
}

function sourceOf({ url, label }: SourceDraft): Source | null {
  return url === "" && label === ""
    ? null
    : { kind: "link", url: url.trim(), label: label.trim() };
}

/** The desktop app opens only https links in the browser. */
function isSourceLink(url: string): boolean {
  return URL.parse(url)?.protocol === "https:";
}

interface SourceLinkProps {
  source: Source;
}

function SourceLink({ source }: SourceLinkProps) {
  return (
    <Anchor
      href={source.url}
      target="_blank"
      rel="noreferrer"
      size="sm"
      style={{ overflowWrap: "anywhere" }}
    >
      {source.label}
    </Anchor>
  );
}
