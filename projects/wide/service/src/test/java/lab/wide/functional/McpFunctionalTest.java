package lab.wide.functional;

import static org.assertj.core.api.Assertions.assertThat;

import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lab.test.RequiresDocker;
import lab.wide.api.RefinementTools;
import lab.wide.api.SignalTools;
import lab.wide.domain.SignalRepository;
import lab.wide.testing.ServerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@RequiresDocker
class McpFunctionalTest extends ServerTestBase {

  @Autowired SignalRepository signalRepository;

  @Test
  void listTools_connectedClient_describesEveryTool() {
    var tools = mcpClient().listTools().tools();

    assertThat(tools).hasSize(22);
    assertThat(tools).allSatisfy(tool -> assertThat(tool.description()).isNotBlank());
  }

  @Test
  void listTools_connectedClient_marksOnlyMandatoryParametersRequired() {
    var tools = mcpClient().listTools().tools();

    var captureSignal = tool(tools, SignalTools.CAPTURE_SIGNAL);
    assertThat(map(captureSignal.inputSchema().get("properties")).keySet())
        .isEqualTo(Set.of("title", "content", "source"));
    assertThat(captureSignal.inputSchema().get("required")).isEqualTo(List.of("title", "content"));
    assertThat(tool(tools, RefinementTools.CREATE_SOLUTION).inputSchema().get("required"))
        .isEqualTo(List.of("title", "approach"));
  }

  @Test
  void callTool_validWrite_returnsOnlyTheSavedId() {
    var content = randomString();

    var callToolResult =
        callTool(SignalTools.CAPTURE_SIGNAL, Map.of("title", randomString(), "content", content));

    var signal = signalRepository.findAll().iterator().next();
    assertThat(callToolResult.isError()).isNotEqualTo(true);
    assertThat(text(callToolResult)).isEqualTo("{\"id\":\"" + signal.id() + "\"}");
    assertThat(signal.content()).isEqualTo(content);
  }

  @Test
  void callTool_invalidWrite_returnsTheValidationErrorWithoutWriting() {
    var callToolResult =
        callTool(SignalTools.CAPTURE_SIGNAL, Map.of("title", randomString(), "content", " \n"));

    assertThat(callToolResult.isError()).isTrue();
    assertThat(text(callToolResult)).contains("Nonblank content is required.");
    assertThat(signalRepository.count()).isZero();
  }

  @Test
  void callTool_unknownId_returnsNotFoundError() {
    var id = UUID.randomUUID();

    var callToolResult = callTool(RefinementTools.GET_PROBLEM, Map.of("id", id.toString()));

    assertThat(callToolResult.isError()).isTrue();
    assertThat(text(callToolResult)).contains("Problem not found: " + id);
  }

  private static Tool tool(List<Tool> tools, String name) {
    return tools.stream().filter(tool -> tool.name().equals(name)).findFirst().orElseThrow();
  }
}
