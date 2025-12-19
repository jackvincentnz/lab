package lab.mops.ai.infrastructure.llm;

public interface ToolApprovalPolicy {
  boolean needsApproval(String toolName);
}
