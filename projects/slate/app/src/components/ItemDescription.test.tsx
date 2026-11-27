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
import type { Item } from "../api";
import { item } from "../test/fakes";
import { ItemDescription } from "./ItemDescription";

type ItemDescriptionProps = Parameters<typeof ItemDescription>[0];
type ChangeState = Pick<ItemDescriptionProps, "disabled" | "saving">;

const idle: ChangeState = { disabled: false, saving: false };

function renderDescription(
  shownItem: Item = item({ description: "Before Friday" }),
) {
  const handlers = {
    onDescribe: vi.fn<ItemDescriptionProps["onDescribe"]>(),
    onDescribeCancel: vi.fn<ItemDescriptionProps["onDescribeCancel"]>(),
  };
  const description = (shown: Item, state: ChangeState) => (
    <ItemDescription item={shown} {...state} {...handlers} />
  );
  const { rerender } = render(description(shownItem, idle));
  return {
    ...handlers,
    disable: () =>
      rerender(description(shownItem, { ...idle, disabled: true })),
    startSaving: () =>
      rerender(description(shownItem, { ...idle, saving: true })),
    showItem: (shown: Item) => rerender(description(shown, idle)),
  };
}

function saveCallbacks(onDescribe: Mock<ItemDescriptionProps["onDescribe"]>) {
  const [, , callbacks] = onDescribe.mock.lastCall!;
  return callbacks;
}

async function startEdit(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByRole("button", { name: "Edit description" }));
  return screen.getByRole("textbox", { name: "Markdown" });
}

describe("ItemDescription", () => {
  it("shows the description as Markdown", () => {
    renderDescription(item({ description: "**Why:** it blocks\n\n- Ask" }));

    expect(screen.getByText("Why:").tagName).toBe("STRONG");
    expect(screen.getByRole("listitem")).toHaveTextContent("Ask");
  });

  it("opens a link in a new window without a referrer", () => {
    renderDescription(
      item({ description: "See [the RFC](https://example.com/rfc)" }),
    );

    const link = screen.getByRole("link", { name: "the RFC" });

    expect(link).toHaveAttribute("href", "https://example.com/rfc");
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noreferrer");
  });

  it("leaves out raw HTML", () => {
    renderDescription(item({ description: "Plan <button>Go</button>" }));

    expect(
      screen.queryByRole("button", { name: "Go" }),
    ).not.toBeInTheDocument();
  });

  it("offers to add a description when there is none", async () => {
    const user = userEvent.setup();
    renderDescription(item({ description: "" }));

    await user.click(screen.getByRole("button", { name: "Add a description" }));

    expect(screen.getByRole("textbox", { name: "Markdown" })).toHaveValue("");
    expect(
      screen.queryByRole("button", { name: "Edit description" }),
    ).not.toBeInTheDocument();
  });

  it("opens the Markdown for editing with the caret at its end", async () => {
    const user = userEvent.setup();
    renderDescription(item({ description: "**Before** Friday" }));

    const markdown = await startEdit(user);

    expect(markdown).toHaveValue("**Before** Friday");
    expect(markdown).toHaveFocus();
    expect(markdown).toHaveProperty("selectionStart", 17);
  });

  it("closes the editor and returns focus to Edit once the description is saved", async () => {
    const user = userEvent.setup();
    const original = item({ description: "Before Friday" });
    const { onDescribe } = renderDescription(original);
    onDescribe.mockImplementation((_editedFrom, description, { onSaved }) =>
      onSaved({ ...original, description }),
    );
    const markdown = await startEdit(user);
    await user.type(markdown, " noon");

    await user.click(screen.getByRole("button", { name: "Save" }));

    expect(onDescribe).toHaveBeenCalledWith(
      original,
      "Before Friday noon",
      expect.anything(),
    );
    expect(
      screen.queryByRole("textbox", { name: "Markdown" }),
    ).not.toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Edit description" }),
    ).toHaveFocus();
  });

  it("saves with Cmd+Enter", async () => {
    const user = userEvent.setup();
    const original = item({ description: "Before Friday" });
    const { onDescribe } = renderDescription(original);
    const markdown = await startEdit(user);

    await user.type(markdown, " noon{Meta>}{Enter}{/Meta}");

    expect(onDescribe).toHaveBeenCalledWith(
      original,
      "Before Friday noon",
      expect.anything(),
    );
  });

  it("starts a new line with Enter", async () => {
    const user = userEvent.setup();
    const { onDescribe } = renderDescription();
    const markdown = await startEdit(user);

    await user.type(markdown, "{Enter}- Ask");

    expect(onDescribe).not.toHaveBeenCalled();
    expect(markdown).toHaveValue("Before Friday\n- Ask");
  });

  it("keeps the editor open until the description is saved", async () => {
    const user = userEvent.setup();
    renderDescription();
    const markdown = await startEdit(user);

    await user.type(markdown, " noon{Meta>}{Enter}{/Meta}");

    expect(markdown).toHaveValue("Before Friday noon");
  });

  it("keeps the draft and shows the current description when it changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ description: "Before Friday" });
    const { onDescribe, showItem } = renderDescription(original);
    const markdown = await startEdit(user);
    await user.type(markdown, " noon{Meta>}{Enter}{/Meta}");
    act(() => saveCallbacks(onDescribe).onChanged());

    showItem({
      ...original,
      description: "Before **Thursday**",
      updatedAt: "2026-11-22T10:00:00.000Z",
    });

    expect(markdown).toHaveValue("Before Friday noon");
    expect(
      screen.getByText("Changed elsewhere. Current description:"),
    ).toBeInTheDocument();
    expect(screen.getByText("Thursday")).toBeInTheDocument();
  });

  it("says when the description was removed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ description: "Before Friday" });
    const { onDescribe, showItem } = renderDescription(original);
    const markdown = await startEdit(user);
    await user.type(markdown, " noon{Meta>}{Enter}{/Meta}");
    act(() => saveCallbacks(onDescribe).onChanged());

    showItem({
      ...original,
      description: "",
      updatedAt: "2026-11-22T10:00:00.000Z",
    });

    expect(
      screen.getByText("Changed elsewhere. The description is now empty."),
    ).toBeInTheDocument();
  });

  it("shows no current description until a copy newer than the refused one arrives", async () => {
    const user = userEvent.setup();
    const { onDescribe } = renderDescription();
    const markdown = await startEdit(user);
    await user.type(markdown, " noon{Meta>}{Enter}{/Meta}");

    act(() => saveCallbacks(onDescribe).onChanged());

    expect(screen.getByText("Changed elsewhere.")).toBeInTheDocument();
  });

  it("cancels an edit with Escape and returns focus to Edit", async () => {
    const user = userEvent.setup();
    const { onDescribe, onDescribeCancel } = renderDescription();
    const markdown = await startEdit(user);

    await user.type(markdown, " noon{Escape}");

    expect(onDescribe).not.toHaveBeenCalled();
    expect(onDescribeCancel).toHaveBeenCalled();
    expect(screen.getByText("Before Friday")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Edit description" }),
    ).toHaveFocus();
  });

  it("cancels an edit with the Cancel button", async () => {
    const user = userEvent.setup();
    const { onDescribeCancel } = renderDescription();
    await startEdit(user);

    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onDescribeCancel).toHaveBeenCalled();
    expect(
      screen.queryByRole("textbox", { name: "Markdown" }),
    ).not.toBeInTheDocument();
  });

  it("keeps focus on Save when disabled", async () => {
    const user = userEvent.setup();
    const { disable } = renderDescription();
    await startEdit(user);
    const save = screen.getByRole("button", { name: "Save" });
    await user.click(save);

    disable();

    expect(save).toHaveFocus();
    expect(save).toHaveAttribute("aria-disabled", "true");
  });

  it("takes no cancel while a change saves", async () => {
    const user = userEvent.setup();
    const { onDescribeCancel, startSaving } = renderDescription();
    const markdown = await startEdit(user);
    await user.type(markdown, "{Meta>}{Enter}{/Meta}");
    startSaving();

    await user.type(markdown, "{Escape}");
    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onDescribeCancel).not.toHaveBeenCalled();
    expect(markdown).toBeInTheDocument();
  });
});
