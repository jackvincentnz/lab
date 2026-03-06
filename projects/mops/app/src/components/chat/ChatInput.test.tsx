import {
  describe,
  expect,
  render,
  screen,
  test,
  userEvent,
  vi,
} from "../../test";
import { ChatInput } from "./ChatInput";

describe("ChatInput", () => {
  test("sends on enter without shift", async () => {
    const onSend = vi.fn();

    render(
      <ChatInput
        value="hello"
        onChange={() => undefined}
        onSend={onSend}
        isLoading={false}
      />,
    );

    await userEvent.type(screen.getByRole("textbox"), "{enter}");

    expect(onSend).toHaveBeenCalled();
  });

  test("does not send on shift-enter", async () => {
    const onSend = vi.fn();

    render(
      <ChatInput
        value="hello"
        onChange={() => undefined}
        onSend={onSend}
        isLoading={false}
      />,
    );

    await userEvent.type(
      screen.getByRole("textbox"),
      "{shift>}{enter}{/shift}",
    );

    expect(onSend).not.toHaveBeenCalled();
  });
});
