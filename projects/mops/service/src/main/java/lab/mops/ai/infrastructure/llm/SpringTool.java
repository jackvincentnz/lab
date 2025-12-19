package lab.mops.ai.infrastructure.llm;

import lab.mops.ai.application.chat.Tool;
import lab.mops.ai.application.chat.ToolDefinition;
import org.springframework.ai.tool.ToolCallback;

public class SpringTool implements Tool {

  private final ToolCallback delegate;

  private final ToolDefinition toolDefinition;

  public SpringTool(ToolCallback delegate, ToolApprovalPolicy approvalPolicy) {
    this.delegate = delegate;
    this.toolDefinition = map(delegate.getToolDefinition(), approvalPolicy);
  }

  @Override
  public ToolDefinition getToolDefinition() {
    return this.toolDefinition;
  }

  @Override
  public String call(String toolInput) {
    return this.delegate.call(toolInput);
  }

  private static ToolDefinition map(
      org.springframework.ai.tool.definition.ToolDefinition toolDefinition,
      ToolApprovalPolicy approvalPolicy) {
    return new ToolDefinition() {
      @Override
      public String name() {
        return toolDefinition.name();
      }

      @Override
      public String description() {
        return toolDefinition.description();
      }

      @Override
      public boolean needsApproval() {
        return approvalPolicy.needsApproval(toolDefinition.name());
      }

      @Override
      public String inputSchema() {
        return toolDefinition.inputSchema();
      }
    };
  }
}
