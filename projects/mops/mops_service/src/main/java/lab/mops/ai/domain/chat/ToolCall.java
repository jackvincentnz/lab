package lab.mops.ai.domain.chat;

public record ToolCall(String id, String name, String arguments, ToolCallStatus status) {}
