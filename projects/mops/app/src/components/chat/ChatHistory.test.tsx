import { MockedResponse } from "@apollo/client/testing";
import {
  describe,
  expect,
  render,
  screen,
  test,
  userEvent,
  vi,
} from "../../test";
import { AllChatsDocument, AllChatsQuery } from "../../__generated__/graphql";
import { ChatHistory } from "./ChatHistory";

describe("ChatHistory", () => {
  test("renders an empty state", async () => {
    render(
      <ChatHistory onSelectChat={() => undefined} onBack={() => undefined} />,
      {
        mockedProvider: {
          mocks: [mockAllChats([])],
        },
      },
    );

    expect(await screen.findByText("No previous chats")).toBeVisible();
  });

  test("renders chats and selects one", async () => {
    const onSelectChat = vi.fn();

    render(
      <ChatHistory onSelectChat={onSelectChat} onBack={() => undefined} />,
      {
        mockedProvider: {
          mocks: [
            mockAllChats([
              {
                id: "chat-1",
                createdAt: "2026-03-01T12:00:00.000Z",
                messages: [{ id: "message-1", content: "First prompt" }],
              },
            ]),
          ],
        },
      },
    );

    await userEvent.click(await screen.findByText("First prompt"));

    expect(onSelectChat).toHaveBeenCalledWith("chat-1");
  });

  test("renders an error state", async () => {
    const errorMock: MockedResponse<AllChatsQuery> = {
      request: {
        query: AllChatsDocument,
      },
      error: new Error("boom"),
    };

    render(
      <ChatHistory onSelectChat={() => undefined} onBack={() => undefined} />,
      {
        mockedProvider: {
          mocks: [errorMock],
        },
      },
    );

    expect(await screen.findByText(/something went wrong/i)).toBeVisible();
  });
});

function mockAllChats(
  chats: AllChatsQuery["allChats"],
): MockedResponse<AllChatsQuery> {
  return {
    request: {
      query: AllChatsDocument,
    },
    result: {
      data: {
        allChats: chats,
      },
    },
  };
}
