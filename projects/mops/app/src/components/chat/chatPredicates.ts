import type {
  ChatMessageStatus,
  ChatMessageType,
  ToolCallStatus,
} from "../../__generated__/graphql";

interface MessageTypeFields {
  type: ChatMessageType;
}

interface MessageStatusFields {
  status: ChatMessageStatus;
}

interface MessageStateFields {
  type: ChatMessageType;
  status: ChatMessageStatus;
}

interface ToolCallStatusFields {
  status: ToolCallStatus;
}

export function isUserMessageType(type: ChatMessageType) {
  return type === "USER";
}

export function isUserMessage(message: MessageTypeFields) {
  return isUserMessageType(message.type);
}

export function isAssistantMessageType(type: ChatMessageType) {
  return type === "ASSISTANT";
}

export function isAssistantMessage(message: MessageTypeFields) {
  return isAssistantMessageType(message.type);
}

export function isPendingMessageStatus(status: ChatMessageStatus | undefined) {
  return status === "PENDING";
}

export function isPendingMessage(message: MessageStatusFields) {
  return isPendingMessageStatus(message.status);
}

export function isFailedMessageStatus(status: ChatMessageStatus | undefined) {
  return status === "FAILED";
}

export function isFailedMessage(message: MessageStatusFields) {
  return isFailedMessageStatus(message.status);
}

export function isCompletedMessageStatus(
  status: ChatMessageStatus | undefined,
) {
  return status === "COMPLETED";
}

export function isCompletedMessage(message: MessageStatusFields) {
  return isCompletedMessageStatus(message.status);
}

export function isPendingAssistantMessage(message: MessageStateFields) {
  return isAssistantMessage(message) && isPendingMessage(message);
}

export function isPendingApprovalToolCall(toolCall: ToolCallStatusFields) {
  return toolCall.status === "PENDING_APPROVAL";
}

export function isApprovedToolCall(toolCall: ToolCallStatusFields) {
  return toolCall.status === "APPROVED";
}
