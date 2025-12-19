package lab.mops.ai.application.chat;

public interface ToolDefinition {

  String name();

  String description();

  boolean needsApproval();

  String inputSchema();
}
