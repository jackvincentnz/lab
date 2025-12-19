package lab.mops.ai.infrastructure.llm;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
class EnvironmentToolApprovalPolicy implements ToolApprovalPolicy {

  private static final String NEEDS_APPROVAL_FORMAT = "mops.ai.tools.%s.needs-approval";

  private final Environment environment;

  EnvironmentToolApprovalPolicy(Environment environment) {
    this.environment = environment;
  }

  @Override
  public boolean needsApproval(String toolName) {
    return environment.getProperty(getKey(toolName), Boolean.class, true);
  }

  protected static String getKey(String toolName) {
    return String.format(NEEDS_APPROVAL_FORMAT, toolName);
  }
}
