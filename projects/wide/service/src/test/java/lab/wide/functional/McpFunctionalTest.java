package lab.wide.functional;

import static org.assertj.core.api.Assertions.assertThat;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lab.wide.infrastructure.SignalRepository;
import lab.wide.testing.ServerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class McpFunctionalTest extends ServerTest {

  @Autowired SignalRepository signals;

  private McpSyncClient client;

  @BeforeEach
  void connect() {
    signals.deleteAll();
    var transport = HttpClientStreamableHttpTransport.builder(baseUrl()).endpoint("/mcp").build();
    client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(15)).build();
    client.initialize();
  }

  @AfterEach
  void disconnect() {
    client.close();
  }

  @Test
  void listTools_describesRequiredParametersOfEveryTool() {
    var tools = client.listTools().tools();

    assertThat(tools).hasSize(22);
    var capture = tool(tools, "wide_capture_signal");
    assertThat(((Map<?, ?>) capture.inputSchema().get("properties")).keySet())
        .isEqualTo(Set.of("title", "content", "source"));
    assertThat(capture.inputSchema().get("required")).isEqualTo(List.of("title", "content"));
    assertThat(tool(tools, "wide_create_solution").inputSchema().get("required"))
        .isEqualTo(List.of("title", "approach"));
  }

  @Test
  void callTool_capturesASignalAndReadsItBackByTheReceiptId() {
    var title = randomString();
    var content = randomString();

    var saved = call("wide_capture_signal", Map.of("title", title, "content", content));

    var signal = signals.findAll().iterator().next();
    assertThat(saved.isError()).isNotEqualTo(true);
    assertThat(text(saved)).isEqualTo("{\"id\":\"" + signal.id() + "\"}");
    assertThat(signal.content()).isEqualTo(content);
    var fetched = call("wide_get_signal", Map.of("id", signal.id().toString()));
    assertThat(fetched.isError()).isNotEqualTo(true);
    assertThat(text(fetched)).contains(title, content);
    var listed = call("wide_search", Map.of("query", title));
    assertThat(listed.isError()).isNotEqualTo(true);
    assertThat(text(listed)).contains(signal.id().toString(), "hasMore");
  }

  @Test
  void callTool_reportsValidationErrorsWithoutWriting() {
    var rejected = call("wide_capture_signal", Map.of("title", randomString(), "content", " \n"));

    assertThat(rejected.isError()).isTrue();
    assertThat(text(rejected)).contains("Nonblank content is required.");
    assertThat(signals.count()).isZero();
  }

  private McpSchema.CallToolResult call(String tool, Map<String, Object> arguments) {
    return client.callTool(new McpSchema.CallToolRequest(tool, arguments));
  }

  private static McpSchema.Tool tool(List<McpSchema.Tool> tools, String name) {
    return tools.stream().filter(t -> t.name().equals(name)).findFirst().orElseThrow();
  }

  private static String text(McpSchema.CallToolResult result) {
    return ((McpSchema.TextContent) result.content().get(0)).text();
  }
}
