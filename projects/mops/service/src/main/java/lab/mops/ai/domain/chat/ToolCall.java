package lab.mops.ai.domain.chat;

public record ToolCall(ToolCallId id, String name, String arguments, ToolCallStatus status) {}
