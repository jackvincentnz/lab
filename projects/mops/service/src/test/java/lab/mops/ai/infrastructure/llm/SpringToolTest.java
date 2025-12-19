package lab.mops.ai.infrastructure.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

@ExtendWith(MockitoExtension.class)
class SpringToolTest extends TestBase {

  @Mock ToolCallback toolCallback;
  @Mock ToolApprovalPolicy approvalPolicy;

  ToolDefinition toolDefinition;

  SpringTool springTool;

  @BeforeEach
  public void setup() {
    toolDefinition = new DefaultToolDefinition(randomString(), randomString(), randomString());
    when(toolCallback.getToolDefinition()).thenReturn(toolDefinition);
    springTool = new SpringTool(toolCallback, approvalPolicy);
  }

  @Test
  void getToolDefinition_returnsDelegatedName() {
    var result = springTool.getToolDefinition();

    assertThat(result.name()).isEqualTo(toolDefinition.name());
  }

  @Test
  void getToolDefinition_returnsDelegatedDescription() {
    var result = springTool.getToolDefinition();

    assertThat(result.description()).isEqualTo(toolDefinition.description());
  }

  @Test
  void getToolDefinition_returnsDelegatedSchema() {
    var result = springTool.getToolDefinition();

    assertThat(result.inputSchema()).isEqualTo(toolDefinition.inputSchema());
  }

  @Test
  void getToolDefinition_doesNotNeedApproval() {
    var result = springTool.getToolDefinition();

    assertThat(result.needsApproval()).isFalse();
  }

  @Test
  void getToolDefinition_needsApprovalWhenPolicySaysSo() {
    when(approvalPolicy.needsApproval(toolDefinition.name())).thenReturn(true);
    var result = new SpringTool(toolCallback, approvalPolicy).getToolDefinition();

    assertThat(result.needsApproval()).isTrue();
  }

  @Test
  void call_delegatesToToolCallback() {
    var input = randomString();

    springTool.call(input);

    verify(toolCallback).call(input);
  }
}
