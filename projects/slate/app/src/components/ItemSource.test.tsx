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
import { ItemSource } from "./ItemSource";

type ItemSourceProps = Parameters<typeof ItemSource>[0];
type ChangeState = Pick<ItemSourceProps, "disabled" | "saving">;

const idle: ChangeState = { disabled: false, saving: false };

const session: Source = {
  kind: "link",
  url: "https://example.com/session",
  label: "Planning session",
};

function renderSource(shownItem: Item = item({ source: session })) {
  const handlers = {
    onSourceChange: vi.fn<ItemSourceProps["onSourceChange"]>(),
    onSourceChangeCancel: vi.fn<ItemSourceProps["onSourceChangeCancel"]>(),
  };
  const source = (shown: Item, state: ChangeState) => (
    <ItemSource item={shown} {...state} {...handlers} />
  );
  const { rerender } = render(source(shownItem, idle));
  return {
    ...handlers,
    disableWhileSaving: () =>
      rerender(source(shownItem, { disabled: true, saving: true })),
    startSaving: () => rerender(source(shownItem, { ...idle, saving: true })),
    showItem: (shown: Item) => rerender(source(shown, idle)),
  };
}

function saveCallbacks(
  onSourceChange: Mock<ItemSourceProps["onSourceChange"]>,
) {
  const [, , callbacks] = onSourceChange.mock.lastCall!;
  return callbacks;
}

const notes: Source = {
  kind: "reference",
  text: "notes/TODO.md:12",
  label: "TODO list",
};

async function startEdit(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByRole("button", { name: "Edit source" }));
  return {
    link: screen.getByRole("textbox", { name: "Link" }),
    label: screen.getByRole("textbox", { name: "Label" }),
  };
}

describe("ItemSource", () => {
  it("opens the source's link in a new window without a referrer", () => {
    renderSource();

    const link = screen.getByRole("link", { name: "Planning session" });

    expect(link).toHaveAttribute("href", "https://example.com/session");
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noreferrer");
  });

  it("shows a reference as text without a link", () => {
    renderSource(item({ source: notes }));

    expect(screen.getByText("TODO list")).toBeInTheDocument();
    expect(screen.getByText("notes/TODO.md:12")).toBeInTheDocument();
    expect(screen.queryByRole("link")).not.toBeInTheDocument();
  });

  it("selects a reference's whole text with one click, ready to copy", () => {
    renderSource(item({ source: notes }));

    expect(screen.getByText("notes/TODO.md:12")).toHaveStyle({
      userSelect: "all",
    });
  });

  it("opens a claude reference in a new window without a referrer", () => {
    renderSource(
      item({
        source: {
          kind: "reference",
          text: "claude://example/session",
          label: "Claude Desktop session",
        },
      }),
    );

    const link = screen.getByRole("link", { name: "Claude Desktop session" });

    expect(link).toHaveAttribute("href", "claude://example/session");
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noreferrer");
  });

  it("offers to add a source when there is none", async () => {
    const user = userEvent.setup();
    renderSource(item({ source: null }));

    await user.click(screen.getByRole("button", { name: "Add a source" }));

    expect(screen.getByRole("textbox", { name: "Link" })).toHaveValue("");
    expect(screen.getByRole("textbox", { name: "Link" })).toHaveFocus();
    expect(screen.getByRole("textbox", { name: "Label" })).toHaveValue("");
    expect(
      screen.queryByRole("button", { name: "Remove source" }),
    ).not.toBeInTheDocument();
  });

  it("opens the link and label for editing", async () => {
    const user = userEvent.setup();
    renderSource();

    const { link, label } = await startEdit(user);

    expect(link).toHaveValue("https://example.com/session");
    expect(label).toHaveValue("Planning session");
  });

  it("opens a reference for editing", async () => {
    const user = userEvent.setup();
    renderSource(item({ source: notes }));

    await user.click(screen.getByRole("button", { name: "Edit source" }));

    expect(screen.getByRole("radio", { name: "Reference" })).toBeChecked();
    expect(screen.getByRole("textbox", { name: "Reference" })).toHaveValue(
      "notes/TODO.md:12",
    );
    expect(screen.getByRole("textbox", { name: "Label" })).toHaveValue(
      "TODO list",
    );
  });

  it("adds a trimmed reference", async () => {
    const user = userEvent.setup();
    const original = item({ source: null });
    const { onSourceChange } = renderSource(original);
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

    expect(onSourceChange).toHaveBeenCalledWith(
      original,
      notes,
      expect.anything(),
    );
  });

  it("keeps what was typed when the kind changes", async () => {
    const user = userEvent.setup();
    const original = item({ source: session });
    const { onSourceChange } = renderSource(original);
    const { link } = await startEdit(user);
    await user.clear(link);
    await user.type(link, "claude://example/session");

    await user.click(screen.getByRole("radio", { name: "Reference" }));
    await user.click(screen.getByRole("button", { name: "Save" }));

    expect(
      screen.queryByText("Give a link that starts with https://"),
    ).not.toBeInTheDocument();
    expect(onSourceChange).toHaveBeenCalledWith(
      original,
      {
        kind: "reference",
        text: "claude://example/session",
        label: "Planning session",
      },
      expect.anything(),
    );
  });

  it("refuses to save a blank reference", async () => {
    const user = userEvent.setup();
    const { onSourceChange } = renderSource(item({ source: notes }));
    await user.click(screen.getByRole("button", { name: "Edit source" }));
    const reference = screen.getByRole("textbox", { name: "Reference" });
    await user.clear(reference);

    await user.type(reference, "  {Enter}");

    expect(onSourceChange).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
  });

  it("refuses to save a reference over 1000 characters", async () => {
    const user = userEvent.setup();
    const { onSourceChange } = renderSource(item({ source: notes }));
    await user.click(screen.getByRole("button", { name: "Edit source" }));
    const reference = screen.getByRole("textbox", { name: "Reference" });
    await user.clear(reference);
    await user.click(reference);

    await user.paste("a".repeat(1001));
    await user.keyboard("{Enter}");

    expect(onSourceChange).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
    expect(
      screen.getByText("Give a reference of at most 1000 characters"),
    ).toBeInTheDocument();
  });

  it("saves the trimmed source, closes the editor and returns focus to Edit", async () => {
    const user = userEvent.setup();
    const original = item({ source: session });
    const { onSourceChange } = renderSource(original);
    const changed: Source = {
      kind: "link",
      url: "https://example.com/pull/1",
      label: "Pull request",
    };
    onSourceChange.mockImplementation((_editedFrom, source, { onSaved }) =>
      onSaved({ ...original, source }),
    );
    const { link, label } = await startEdit(user);

    await user.clear(link);
    await user.type(link, ` ${changed.url} `);
    await user.clear(label);
    await user.type(label, ` ${changed.label} {Enter}`);

    expect(onSourceChange).toHaveBeenCalledWith(
      original,
      changed,
      expect.anything(),
    );
    expect(
      screen.queryByRole("textbox", { name: "Link" }),
    ).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Edit source" })).toHaveFocus();
  });

  it.each(["http://example.com/session", "javascript:alert(1)", "example"])(
    "refuses to save the link %s",
    async (url) => {
      const user = userEvent.setup();
      const { onSourceChange } = renderSource();
      const { link } = await startEdit(user);
      await user.clear(link);

      await user.type(link, `${url}{Enter}`);

      expect(onSourceChange).not.toHaveBeenCalled();
      expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
      expect(
        screen.getByText("Give a link that starts with https://"),
      ).toBeInTheDocument();
    },
  );

  it("refuses to save a blank label", async () => {
    const user = userEvent.setup();
    const { onSourceChange } = renderSource();
    const { label } = await startEdit(user);
    await user.clear(label);

    await user.type(label, "  {Enter}");

    expect(onSourceChange).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Save" })).toBeDisabled();
  });

  it("removes the source", async () => {
    const user = userEvent.setup();
    const original = item({ source: session });
    const { onSourceChange } = renderSource(original);
    await startEdit(user);

    await user.click(screen.getByRole("button", { name: "Remove source" }));

    expect(onSourceChange).toHaveBeenCalledWith(
      original,
      null,
      expect.anything(),
    );
  });

  it("saves from the latest copy when only another field changed since the edit began", async () => {
    const user = userEvent.setup();
    const original = item({ source: session });
    const { onSourceChange, showItem } = renderSource(original);
    const { label } = await startEdit(user);
    const started = {
      ...original,
      source: { ...session },
      status: "in_progress" as const,
      updatedAt: "2026-11-22T10:00:00.000Z",
    };
    showItem(started);

    await user.type(label, " notes{Enter}");

    expect(onSourceChange).toHaveBeenCalledWith(
      started,
      { ...session, label: "Planning session notes" },
      expect.anything(),
    );
  });

  it("keeps the draft and shows the current source when it changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ source: session });
    const { onSourceChange, showItem } = renderSource(original);
    const { label } = await startEdit(user);
    await user.type(label, " notes{Enter}");
    act(() => saveCallbacks(onSourceChange).onChanged());

    showItem({
      ...original,
      source: { ...session, label: "Retro" },
      updatedAt: "2026-11-22T10:00:00.000Z",
    });

    expect(label).toHaveValue("Planning session notes");
    expect(
      screen.getByText(/Changed elsewhere. Current source:/),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Retro" })).toBeInTheDocument();
  });

  it("shows the current reference when it changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ source: notes });
    const { onSourceChange, showItem } = renderSource(original);
    await user.click(screen.getByRole("button", { name: "Edit source" }));
    await user.type(screen.getByRole("textbox", { name: "Label" }), "{Enter}");
    act(() => saveCallbacks(onSourceChange).onChanged());

    showItem({
      ...original,
      source: { ...notes, text: "notes/plan.md" },
      updatedAt: "2026-11-22T10:00:00.000Z",
    });

    expect(
      screen.getByText(/Changed elsewhere. Current source:/),
    ).toHaveTextContent("notes/plan.md");
  });

  it("says when the source was removed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ source: session });
    const { onSourceChange, showItem } = renderSource(original);
    const { label } = await startEdit(user);
    await user.type(label, " notes{Enter}");
    act(() => saveCallbacks(onSourceChange).onChanged());

    showItem({
      ...original,
      source: null,
      updatedAt: "2026-11-22T10:00:00.000Z",
    });

    expect(
      screen.getByText("Changed elsewhere. The item now has no source."),
    ).toBeInTheDocument();
  });

  it("cancels an edit with Escape and returns focus to Edit", async () => {
    const user = userEvent.setup();
    const { onSourceChange, onSourceChangeCancel } = renderSource();
    const { label } = await startEdit(user);

    await user.type(label, " notes{Escape}");

    expect(onSourceChange).not.toHaveBeenCalled();
    expect(onSourceChangeCancel).toHaveBeenCalled();
    expect(
      screen.getByRole("link", { name: "Planning session" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Edit source" })).toHaveFocus();
  });

  it("keeps focus on Save when disabled while it saves", async () => {
    const user = userEvent.setup();
    const { disableWhileSaving } = renderSource();
    await startEdit(user);
    const save = screen.getByRole("button", { name: "Save" });
    await user.click(save);

    disableWhileSaving();

    expect(save).toHaveFocus();
    expect(save).toHaveAttribute("aria-disabled", "true");
  });

  it("takes no cancel while a change saves", async () => {
    const user = userEvent.setup();
    const { onSourceChangeCancel, startSaving } = renderSource();
    const { label } = await startEdit(user);
    await user.type(label, "{Enter}");
    startSaving();

    await user.type(label, "{Escape}");
    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onSourceChangeCancel).not.toHaveBeenCalled();
    expect(label).toBeInTheDocument();
  });
});
