package lab.mops.ai.application.chat.completions;

public record ToolCall(String id, String toolName, String arguments) {}
