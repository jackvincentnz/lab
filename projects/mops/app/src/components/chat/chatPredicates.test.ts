import { describe, expect, test } from "../../test";
import type {
  ChatMessageStatus,
  ChatMessageType,
  ToolCallStatus,
} from "../../__generated__/graphql";
import {
  isApprovedToolCall,
  isAssistantMessage,
  isAssistantMessageType,
  isCompletedMessage,
  isCompletedMessageStatus,
  isFailedMessage,
  isFailedMessageStatus,
  isPendingApprovalToolCall,
  isPendingAssistantMessage,
  isPendingMessage,
  isPendingMessageStatus,
  isUserMessage,
  isUserMessageType,
} from "./chatPredicates";

const STATUSES: ChatMessageStatus[] = [
  "PENDING",
  "COMPLETED",
  "FAILED",
  "CANCELLED",
];

describe("chat predicates", () => {
  test.each<[ChatMessageType, boolean, boolean]>([
    ["USER", true, false],
    ["ASSISTANT", false, true],
  ])("classifies %s messages", (type, isUser, isAssistant) => {
    expect(isUserMessageType(type)).toBe(isUser);
    expect(isUserMessage({ type })).toBe(isUser);
    expect(isAssistantMessageType(type)).toBe(isAssistant);
    expect(isAssistantMessage({ type })).toBe(isAssistant);
  });

  test.each(STATUSES)("classifies %s messages by status", (status) => {
    expect(isPendingMessageStatus(status)).toBe(status === "PENDING");
    expect(isPendingMessage({ status })).toBe(status === "PENDING");
    expect(isFailedMessageStatus(status)).toBe(status === "FAILED");
    expect(isFailedMessage({ status })).toBe(status === "FAILED");
    expect(isCompletedMessageStatus(status)).toBe(status === "COMPLETED");
    expect(isCompletedMessage({ status })).toBe(status === "COMPLETED");
  });

  test("treats a missing status as no status", () => {
    expect(isPendingMessageStatus(undefined)).toBe(false);
    expect(isFailedMessageStatus(undefined)).toBe(false);
    expect(isCompletedMessageStatus(undefined)).toBe(false);
  });

  test("detects a pending assistant message", () => {
    expect(
      isPendingAssistantMessage({ type: "ASSISTANT", status: "PENDING" }),
    ).toBe(true);
  });

  test.each<[ChatMessageType, ChatMessageStatus]>([
    ["USER", "PENDING"],
    ["ASSISTANT", "COMPLETED"],
    ["ASSISTANT", "FAILED"],
    ["ASSISTANT", "CANCELLED"],
  ])(
    "does not treat a %s %s message as a pending assistant message",
    (type, status) => {
      expect(isPendingAssistantMessage({ type, status })).toBe(false);
    },
  );

  test.each<[ToolCallStatus, boolean, boolean]>([
    ["PENDING_APPROVAL", true, false],
    ["APPROVED", false, true],
    ["REJECTED", false, false],
  ])("classifies %s tool calls", (status, isPendingApproval, isApproved) => {
    expect(isPendingApprovalToolCall({ status })).toBe(isPendingApproval);
    expect(isApprovedToolCall({ status })).toBe(isApproved);
  });
});
