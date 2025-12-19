package lab.mops.ai.application.chat;

public interface Tool {

  ToolDefinition getToolDefinition();

  String call(String toolInput);
}
