import {
  act,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
  type Mock,
} from "@lab/test-utils";
import type { Item, ItemSource as Source } from "../api";
import { item } from "../test/fakes";
import { ItemSources } from "./ItemSources";

type ItemSourcesProps = Parameters<typeof ItemSources>[0];
type ChangeState = Pick<ItemSourcesProps, "disabled" | "saving">;

const idle: ChangeState = { disabled: false, saving: false };

const session: Source = {
  kind: "link",
  url: "https://example.com/session",
  label: "Planning session",
};

const notes: Source = {
  kind: "reference",
  text: "notes/TODO.md:12",
  label: "TODO list",
};

function renderSources(shownItem: Item = item({ sources: [session, notes] })) {
  const handlers = {
    onSourcesChange: vi.fn<ItemSourcesProps["onSourcesChange"]>(),
    onSourcesChangeCancel: vi.fn<ItemSourcesProps["onSourcesChangeCancel"]>(),
  };
  const sources = (shown: Item, state: ChangeState) => (
    <ItemSources item={shown} {...state} {...handlers} />
  );
  const { rerender } = render(sources(shownItem, idle));
  return {
    ...handlers,
    disableWhileSaving: () =>
      rerender(sources(shownItem, { disabled: true, saving: true })),
    startSaving: () => rerender(sources(shownItem, { ...idle, saving: true })),
    showItem: (shown: Item) => rerender(sources(shown, idle)),
  };
}

function saveCallbacks(
  onSourcesChange: Mock<ItemSourcesProps["onSourcesChange"]>,
) {
  const [, , callbacks] = onSourcesChange.mock.lastCall!;
  return callbacks;
}

function editButton(label: string) {
  return screen.getByRole("button", { name: `Edit source ${label}` });
}

async function startEdit(
  user: ReturnType<typeof userEvent.setup>,
  label = session.label,
) {
  await user.click(editButton(label));
  return {
    target: screen.getByRole("textbox", { name: /^(Link|Reference)$/ }),
    label: screen.getByRole("textbox", { name: "Label" }),
  };
}

describe("ItemSources", () => {
  it("shows every source in order", () => {
    renderSources(item({ sources: [notes, session] }));

    const labels = screen
      .getAllByRole("button", { name: /^Edit source / })
      .map((button) => button.getAttribute("aria-label"));

    expect(labels).toEqual([
      "Edit source TODO list",
      "Edit source Planning session",
    ]);
  });

  it("opens a link in a new window without a referrer", () => {
    renderSources();

    const link = screen.getByRole("link", { name: "Planning session" });

    expect(link).toHaveAttribute("href", "https://example.com/session");
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noreferrer");
  });

  it("shows a reference as text without a link", () => {
    renderSources(item({ sources: [notes] }));

    expect(screen.getByText("TODO list")).toBeInTheDocument();
    expect(screen.getByText("notes/TODO.md:12")).toBeInTheDocument();
    expect(screen.queryByRole("link")).not.toBeInTheDocument();
  });

  it("selects a reference's whole text with one click, ready to copy", () => {
    renderSources(item({ sources: [notes] }));

    expect(screen.getByText("notes/TODO.md:12")).toHaveStyle({
      userSelect: "all",
    });
  });

  it("opens a claude reference in a new window without a referrer", () => {
    renderSources(
      item({
        sources: [
          {
            kind: "reference",
            text: "claude://example/session",
            label: "Claude Desktop session",
          },
        ],
      }),
    );

    const link = screen.getByRole("link", { name: "Claude Desktop session" });

    expect(link).toHaveAttribute("href", "claude://example/session");
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noreferrer");
  });

  it("offers to add a source when there is none", async () => {
    const user = userEvent.setup();
    renderSources(item({ sources: [] }));

    await user.click(screen.getByRole("button", { name: "Add a source" }));

    expect(screen.getByRole("textbox", { name: "Label" })).toHaveValue("");
    expect(screen.getByRole("textbox", { name: "Label" })).toHaveFocus();
    expect(screen.getByRole("textbox", { name: "Link" })).toHaveValue("");
    expect(
      screen.queryByRole("button", { name: "Remove source" }),
    ).not.toBeInTheDocument();
  });

  it("adds a trimmed source after the others", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session] });
    const { onSourcesChange } = renderSources(original);
    await user.click(screen.getByRole("button", { name: "Add a source" }));

    await user.click(screen.getByRole("radio", { name: "Reference" }));
    await user.type(
      screen.getByRole("textbox", { name: "Reference" }),
      ` ${notes.text} `,
    );
    await user.type(
      screen.getByRole("textbox", { name: "Label" }),
      ` ${notes.label} {Enter}`,
    );

    expect(onSourcesChange).toHaveBeenCalledWith(
      original,
      { addSources: [notes] },
      expect.anything(),
    );
  });

  it("asks for the label before the link or reference, as it shows them", async () => {
    const user = userEvent.setup();
    renderSources();

    const { label, target } = await startEdit(user);

    expect(label).toHaveFocus();
    expect(screen.getAllByRole("textbox")).toEqual([label, target]);
  });

  it("opens a source for editing", async () => {
    const user = userEvent.setup();
    renderSources();

    await user.click(editButton(notes.label));

    expect(screen.getByRole("radio", { name: "Reference" })).toBeChecked();
    expect(screen.getByRole("textbox", { name: "Reference" })).toHaveValue(
      "notes/TODO.md:12",
    );
    expect(screen.getByRole("textbox", { name: "Label" })).toHaveValue(
      "TODO list",
    );
  });

  it("replaces an edited source in its place, closes the editor and returns focus to Edit", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session, notes] });
    const { onSourcesChange, showItem } = renderSources(original);
    const changed: Source = {
      kind: "link",
      url: "https://example.com/pull/1",
      label: "Pull request",
    };
    const saved = { ...original, sources: [changed, notes] };
    onSourcesChange.mockImplementation((_editedFrom, _change, { onSaved }) =>
      onSaved(saved),
    );
    const { target, label } = await startEdit(user);
    await user.clear(target);
    await user.type(target, ` ${changed.url} `);
    await user.clear(label);

    await user.type(label, ` ${changed.label} {Enter}`);
    showItem(saved);

    expect(onSourcesChange).toHaveBeenCalledWith(
      original,
      { replaceSource: { from: session.url, to: changed } },
      expect.anything(),
    );
    expect(
      screen.queryByRole("textbox", { name: "Link" }),
    ).not.toBeInTheDocument();
    expect(editButton(changed.label)).toHaveFocus();
  });

  it("replaces the source it edits after a source before it was removed elsewhere", async () => {
    const user = userEvent.setup();
    const third: Source = {
      kind: "link",
      url: "https://example.com/pull/3",
      label: "Third",
    };
    const original = item({ sources: [session, notes, third] });
    const { onSourcesChange, showItem } = renderSources(original);
    const { label } = await startEdit(user, notes.label);
    await user.type(label, " notes{Enter}");
    act(() => saveCallbacks(onSourcesChange).onChanged());
    const changed = {
      ...original,
      sources: [notes, third],
      updatedAt: "2026-11-22T10:00:00.000Z",
    };
    showItem(changed);

    await user.click(screen.getByRole("button", { name: "Save" }));

    expect(
      screen.getByText(/Changed elsewhere. Current source:/),
    ).toHaveTextContent("TODO list");
    expect(onSourcesChange).toHaveBeenLastCalledWith(
      changed,
      {
        replaceSource: {
          from: notes.text,
          to: { ...notes, label: "TODO list notes" },
        },
      },
      expect.anything(),
    );
  });

  it("keeps the editor open when sources before it are removed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session, notes] });
    const { showItem } = renderSources(original);
    const { label } = await startEdit(user, notes.label);
    await user.type(label, " notes");

    showItem({ ...original, sources: [notes] });

    expect(screen.getByRole("textbox", { name: "Label" })).toHaveValue(
      "TODO list notes",
    );
  });

  it("removes a source", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session, notes] });
    const { onSourcesChange } = renderSources(original);
    await startEdit(user, notes.label);

    await user.click(screen.getByRole("button", { name: "Remove source" }));

    expect(onSourcesChange).toHaveBeenCalledWith(
      original,
      { removeSources: [notes.text] },
      expect.anything(),
    );
  });

  it("returns focus to Add a source once a source is removed", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session, notes] });
    const { onSourcesChange, showItem } = renderSources(original);
    const saved = { ...original, sources: [session] };
    onSourcesChange.mockImplementation((_editedFrom, _change, { onSaved }) =>
      onSaved(saved),
    );
    await startEdit(user, notes.label);

    await user.click(screen.getByRole("button", { name: "Remove source" }));
    showItem(saved);

    expect(screen.getByRole("button", { name: "Add a source" })).toHaveFocus();
  });

  it("keeps the field being typed in while a changed link saves", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session, notes] });
    const { onSourcesChange, showItem } = renderSources(original);
    const changed = { ...session, url: "https://example.com/pull/1" };
    const { target, label } = await startEdit(user);
    await user.clear(target);
    await user.type(target, changed.url);
    await user.type(label, "{Enter}");
    await user.type(label, " notes");

    act(() =>
      saveCallbacks(onSourcesChange).onSaved({
        ...original,
        sources: [changed, notes],
      }),
    );
    showItem({ ...original, sources: [changed, notes] });

    expect(label).toBeInTheDocument();
    expect(label).toHaveFocus();
    expect(label).toHaveValue("Planning session notes");
  });

  it.each(["http://example.com/session", "javascript:alert(1)", "example"])(
    "refuses to save the link %s",
    async (url) => {
      const user = userEvent.setup();
      const { onSourcesChange } = renderSources();
      const { target } = await startEdit(user);
      await user.clear(target);

      await user.type(target, `${url}{Enter}`);

      expect(onSourcesChange).not.toHaveBeenCalled();
      expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
      expect(
        screen.getByText("Give a link that starts with https://"),
      ).toBeInTheDocument();
    },
  );

  it("refuses to save a blank label", async () => {
    const user = userEvent.setup();
    const { onSourcesChange } = renderSources();
    const { label } = await startEdit(user);
    await user.clear(label);

    await user.type(label, "  {Enter}");

    expect(onSourcesChange).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
  });

  it("refuses to save a source the item already has", async () => {
    const user = userEvent.setup();
    const { onSourcesChange } = renderSources();
    await user.click(screen.getByRole("button", { name: "Add a source" }));

    await user.type(screen.getByRole("textbox", { name: "Link" }), session.url);
    await user.type(
      screen.getByRole("textbox", { name: "Label" }),
      "Again{Enter}",
    );

    expect(onSourcesChange).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
    expect(
      screen.getByText("The item already has this source"),
    ).toBeInTheDocument();
  });

  it("keeps what was typed when the kind changes", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session] });
    const { onSourcesChange } = renderSources(original);
    const { target } = await startEdit(user);
    await user.clear(target);
    await user.type(target, "claude://example/session");

    await user.click(screen.getByRole("radio", { name: "Reference" }));
    await user.click(screen.getByRole("button", { name: "Save" }));

    expect(
      screen.queryByText("Give a link that starts with https://"),
    ).not.toBeInTheDocument();
    expect(onSourcesChange).toHaveBeenCalledWith(
      original,
      {
        replaceSource: {
          from: session.url,
          to: {
            kind: "reference",
            text: "claude://example/session",
            label: "Planning session",
          },
        },
      },
      expect.anything(),
    );
  });

  it("refuses to save a blank reference", async () => {
    const user = userEvent.setup();
    const { onSourcesChange } = renderSources();
    const { target } = await startEdit(user, notes.label);
    await user.clear(target);

    await user.type(target, "  {Enter}");

    expect(onSourcesChange).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
  });

  it("refuses to save a reference over 1000 characters", async () => {
    const user = userEvent.setup();
    const { onSourcesChange } = renderSources();
    const { target } = await startEdit(user, notes.label);
    await user.clear(target);
    await user.click(target);

    await user.paste("a".repeat(1001));
    await user.keyboard("{Enter}");

    expect(onSourcesChange).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
    expect(
      screen.getByText("Give a reference of at most 1000 characters"),
    ).toBeInTheDocument();
  });

  it("saves from the latest copy when only another field changed since the edit began", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session] });
    const { onSourcesChange, showItem } = renderSources(original);
    const { label } = await startEdit(user);
    const started = {
      ...original,
      sources: [{ ...session }],
      status: "in_progress" as const,
      updatedAt: "2026-11-22T10:00:00.000Z",
    };
    showItem(started);

    await user.type(label, " notes{Enter}");

    expect(onSourcesChange).toHaveBeenCalledWith(
      started,
      {
        replaceSource: {
          from: session.url,
          to: { ...session, label: "Planning session notes" },
        },
      },
      expect.anything(),
    );
  });

  it("keeps the draft and shows the current source when it changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session] });
    const { onSourcesChange, showItem } = renderSources(original);
    const { label } = await startEdit(user);
    await user.type(label, " notes{Enter}");
    act(() => saveCallbacks(onSourcesChange).onChanged());

    showItem({
      ...original,
      sources: [{ ...session, label: "Retro" }],
      updatedAt: "2026-11-22T10:00:00.000Z",
    });

    expect(label).toHaveValue("Planning session notes");
    expect(
      screen.getByText(/Changed elsewhere. Current source:/),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Retro" })).toBeInTheDocument();
  });

  it("adds the draft again when its source was removed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ sources: [session] });
    const { onSourcesChange, showItem } = renderSources(original);
    const { label } = await startEdit(user);
    await user.type(label, " notes{Enter}");
    act(() => saveCallbacks(onSourcesChange).onChanged());
    const removed = {
      ...original,
      sources: [],
      updatedAt: "2026-11-22T10:00:00.000Z",
    };
    showItem(removed);

    await user.click(screen.getByRole("button", { name: "Save" }));

    expect(
      screen.getByText(
        "Changed elsewhere. This source is no longer on the item, so Save adds it.",
      ),
    ).toBeInTheDocument();
    expect(onSourcesChange).toHaveBeenLastCalledWith(
      removed,
      { addSources: [{ ...session, label: "Planning session notes" }] },
      expect.anything(),
    );
  });

  it("cancels an edit with Escape and returns focus to Edit", async () => {
    const user = userEvent.setup();
    const { onSourcesChange, onSourcesChangeCancel } = renderSources();
    const { label } = await startEdit(user);

    await user.type(label, " notes{Escape}");

    expect(onSourcesChange).not.toHaveBeenCalled();
    expect(onSourcesChangeCancel).toHaveBeenCalled();
    expect(
      screen.getByRole("link", { name: "Planning session" }),
    ).toBeInTheDocument();
    expect(editButton(session.label)).toHaveFocus();
  });

  it("keeps focus on Save when disabled while it saves", async () => {
    const user = userEvent.setup();
    const { disableWhileSaving } = renderSources();
    await startEdit(user);
    const save = screen.getByRole("button", { name: "Save" });
    await user.click(save);

    disableWhileSaving();

    expect(save).toHaveFocus();
    expect(save).toHaveAttribute("aria-disabled", "true");
  });

  it("takes no cancel while a change saves", async () => {
    const user = userEvent.setup();
    const { onSourcesChangeCancel, startSaving } = renderSources();
    const { label } = await startEdit(user);
    await user.type(label, "{Enter}");
    startSaving();

    await user.type(label, "{Escape}");
    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onSourcesChangeCancel).not.toHaveBeenCalled();
    expect(label).toBeInTheDocument();
  });
});
