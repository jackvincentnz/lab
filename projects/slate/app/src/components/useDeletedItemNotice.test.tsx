import {
  act,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
  waitFor,
  within,
  type Mock,
} from "@lab/test-utils";
import { Notifications } from "@mantine/notifications";
import { item } from "../test/fakes";
import {
  AUTO_CLOSE_MS,
  type DeletedItem,
  useDeletedItemNotice,
} from "./useDeletedItemNotice";

type Options = Parameters<typeof useDeletedItemNotice>[1];

interface NoticeProps {
  deleted: DeletedItem | undefined;
  disabled: boolean;
  restoring: boolean;
  onUndo: Mock<Options["onUndo"]>;
  onDismiss: Mock<Options["onDismiss"]>;
}

function Notice({ deleted, ...options }: NoticeProps) {
  useDeletedItemNotice(deleted, options);
  return null;
}

function renderNotice(changes: Partial<NoticeProps> = {}) {
  const props: NoticeProps = {
    deleted: { item: item({ name: "Plan the week" }), withKeyboard: false },
    disabled: false,
    restoring: false,
    onUndo: vi.fn<Options["onUndo"]>(),
    onDismiss: vi.fn<Options["onDismiss"]>(),
    ...changes,
  };
  const ui = (latest: NoticeProps) => (
    <>
      <Notice {...latest} />
      <Notifications />
    </>
  );
  const view = render(ui(props));

  return {
    ...props,
    rerender: (next: Partial<NoticeProps>) =>
      view.rerender(ui({ ...props, ...next })),
  };
}

function undo() {
  return screen.queryByRole("button", { name: "Undo" });
}

describe("useDeletedItemNotice", () => {
  it("names the deleted item under a Deleted title", async () => {
    renderNotice();

    const notice = await screen.findByRole("alert");
    expect(within(notice).getByText("Deleted")).toBeInTheDocument();
    expect(within(notice).getByText("Plan the week")).toBeInTheDocument();
  });

  it("reports an undo for the deleted item", async () => {
    const user = userEvent.setup();
    const { deleted, onUndo } = renderNotice();

    await user.click(await screen.findByRole("button", { name: "Undo" }));

    expect(onUndo).toHaveBeenCalledExactlyOnceWith(deleted?.item);
  });

  it("reports a dismiss", async () => {
    const user = userEvent.setup();
    const { onDismiss } = renderNotice();

    await user.click(await screen.findByRole("button", { name: "Dismiss" }));

    expect(onDismiss).toHaveBeenCalledOnce();
  });

  it("closes without reporting a dismiss when the page clears it", async () => {
    const { onDismiss, rerender } = renderNotice();
    await screen.findByRole("button", { name: "Undo" });

    rerender({ deleted: undefined });

    await waitFor(() => expect(undo()).not.toBeInTheDocument());
    expect(onDismiss).not.toHaveBeenCalled();
  });

  it("shows the undo as disabled while the page says so", async () => {
    const { rerender } = renderNotice();
    await screen.findByRole("button", { name: "Undo" });

    rerender({ disabled: true });

    await waitFor(() => expect(undo()).toBeDisabled());
  });

  it("keeps offering the undo until it has been open for the auto-close time", async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    renderNotice();
    await screen.findByRole("button", { name: "Undo" });

    await act(() => vi.advanceTimersByTimeAsync(AUTO_CLOSE_MS - 100));

    expect(undo()).toBeInTheDocument();
  });

  it("closes and reports a dismiss once it has been open for the auto-close time", async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const { onDismiss } = renderNotice();
    await screen.findByRole("button", { name: "Undo" });

    await act(() => vi.advanceTimersByTimeAsync(AUTO_CLOSE_MS + 1_000));

    expect(undo()).not.toBeInTheDocument();
    expect(onDismiss).toHaveBeenCalledOnce();
  });

  it("keeps offering the undo while it restores", async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    renderNotice({ restoring: true });
    await screen.findByRole("button", { name: "Undo" });

    await act(() => vi.advanceTimersByTimeAsync(AUTO_CLOSE_MS * 3));

    expect(undo()).toBeInTheDocument();
  });

  it("closes once the auto-close time passes after a restore stops", async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const { rerender } = renderNotice({ restoring: true });
    await screen.findByRole("button", { name: "Undo" });
    rerender({ restoring: false });

    await act(() => vi.advanceTimersByTimeAsync(AUTO_CLOSE_MS + 1_000));

    expect(undo()).not.toBeInTheDocument();
  });
});
