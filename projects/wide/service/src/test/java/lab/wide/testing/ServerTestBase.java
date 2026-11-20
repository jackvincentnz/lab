package lab.wide.testing;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

/** Boots the service on a random port and talks to it as MCP and GraphQL clients do. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ServerTestBase extends PostgresTestBase {

  private static final Pattern RECEIPT = Pattern.compile("\\{\"id\":\"([0-9a-f-]{36})\"}");

  @Autowired private Environment environment;

  @Autowired private JdbcTemplate jdbcTemplate;

  private McpSyncClient mcpSyncClient;

  @BeforeEach
  void connect() {
    clearRecords(jdbcTemplate);

    var transport = HttpClientStreamableHttpTransport.builder(baseUrl()).endpoint("/mcp").build();
    mcpSyncClient = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(15)).build();
    mcpSyncClient.initialize();
  }

  @AfterEach
  void disconnect() {
    mcpSyncClient.close();
  }

  protected McpSyncClient mcpClient() {
    return mcpSyncClient;
  }

  protected CallToolResult callTool(String name, Map<String, Object> arguments) {
    return mcpSyncClient.callTool(new CallToolRequest(name, arguments));
  }

  protected static String text(CallToolResult callToolResult) {
    return ((TextContent) callToolResult.content().get(0)).text();
  }

  protected static UUID receiptId(CallToolResult callToolResult) {
    var matcher = RECEIPT.matcher(text(callToolResult));
    if (!matcher.matches()) {
      throw new AssertionError("Not a write receipt: " + text(callToolResult));
    }

    return UUID.fromString(matcher.group(1));
  }

  protected Map<?, ?> graphql(String query, Map<String, ?> variables) {
    return RestClient.create(baseUrl())
        .post()
        .uri("/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("query", query, "variables", variables))
        .retrieve()
        .body(Map.class);
  }

  private String baseUrl() {
    return "http://127.0.0.1:" + environment.getProperty("local.server.port");
  }

  protected static Map<?, ?> map(Object value) {
    return (Map<?, ?>) value;
  }

  protected static List<?> list(Object value) {
    return (List<?>) value;
  }
}
