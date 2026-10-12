package lab.mops.ai.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lab.libs.identity.testing.JwkSetServer;
import lab.libs.identity.testing.TestTokens;
import lab.mops.ai.application.chat.ChatEventHandler;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.ToolCall;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
import lab.test.TestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Exercises the actual Mops HTTP, JDBC, security and graphql-transport-ws stack. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "server.address=::1",
      "spring.ai.google.genai.api-key=synthetic-test-key",
      "mops.identity.development.enabled=false"
    })
class ChatSubscriptionIntegrationTest extends TestBase {
  private static final JwkSetServer KEYS = JwkSetServer.start(TestTokens.devJwkSet());
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String FIELDS =
      "id messages { id type status content toolCalls { id name arguments status } }";
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  @LocalServerPort int port;
  @Autowired ChatRepository chats;
  @Autowired PlatformTransactionManager transactions;
  // Make each transition deterministic without contacting a model provider.
  @MockitoBean ChatEventHandler completionHandler;

  @DynamicPropertySource
  static void keys(DynamicPropertyRegistry registry) {
    registry.add("mops.identity.jwk-set-uri", KEYS::uri);
  }

  @AfterAll
  static void closeKeys() {
    KEYS.close();
  }

  @Test
  void chatUpdated_committedCompletionAndReconnect_returnLatestState() throws Exception {
    var chat = startChat();
    var id = chat.path("id").asString();
    try (var stream = connect(id)) {
      var initial = stream.nextChat();
      assertThat(initial.path("messages").get(1).path("status").asString()).isEqualTo("PENDING");
      var saved = chats.getById(ChatId.fromString(id));
      saved.completeMessage(saved.getMessages().get(1).getId(), randomString());
      chats.save(saved);
      var updated = stream.nextChat();
      assertThat(updated.path("messages").get(1).path("content").asString())
          .isEqualTo(saved.getMessages().get(1).getContent().orElseThrow());
      assertThat(updated.path("messages").get(1).path("status").asString()).isEqualTo("COMPLETED");
    }
    try (var reconnected = connect(id)) {
      assertThat(reconnected.nextChat().path("messages").get(1).path("status").asString())
          .isEqualTo("COMPLETED");
    }
  }

  @Test
  void chatUpdated_cancelEditAndRetry_streamReplacementMessages() throws Exception {
    var chat = startChat();
    var id = chat.path("id").asString();
    try (var stream = connect(id)) {
      stream.nextChat();
      mutate("addUserMessage", "{chatId: \"" + id + "\", content: \"second\"}");
      var added = stream.awaitChatWithMessages(4);
      assertThat(added.path("messages").get(1).path("status").asString()).isEqualTo("CANCELLED");
      var userId = added.path("messages").get(0).path("id").asString();
      mutate(
          "editUserMessage",
          "{chatId: \"" + id + "\", messageId: \"" + userId + "\", content: \"edited\"}");
      var edited = stream.awaitChatWithMessages(2);
      assertThat(edited.path("messages").get(0).path("content").asString()).isEqualTo("edited");
      var previousId = edited.path("messages").get(1).path("id").asString();
      mutate(
          "retryAssistantMessage", "{chatId: \"" + id + "\", messageId: \"" + previousId + "\"}");
      assertThat(stream.nextChat().path("messages").get(1).path("id").asString())
          .isNotEqualTo(previousId);
    }
  }

  @Test
  void chatUpdated_approvalExecutionAndRejection_streamToolStates() throws Exception {
    var chat = startChat();
    var id = chat.path("id").asString();
    try (var stream = connect(id)) {
      stream.nextChat();
      var saved = chats.getById(ChatId.fromString(id));
      var messageId = saved.getMessages().get(1).getId();
      var approved = ToolCallId.create();
      var rejected = ToolCallId.create();
      saved.addPendingToolCalls(
          messageId,
          List.of(
              ToolCall.of(approved, randomString(), "{}", ToolCallStatus.PENDING_APPROVAL),
              ToolCall.of(rejected, randomString(), "{}", ToolCallStatus.PENDING_APPROVAL)));
      chats.save(saved);
      assertThat(stream.nextChat().path("messages").get(1).path("toolCalls").size()).isEqualTo(2);
      mutate(
          "approveToolCall",
          "{chatId: \""
              + id
              + "\", messageId: \""
              + messageId
              + "\", toolCallId: \""
              + approved
              + "\"}");
      assertToolStatus(stream.nextChat(), approved, "APPROVED");
      saved = chats.getById(ChatId.fromString(id));
      saved.recordToolResult(messageId, approved, randomString());
      chats.save(saved);
      assertToolStatus(stream.nextChat(), approved, "APPROVED");
      mutate(
          "rejectToolCall",
          "{chatId: \""
              + id
              + "\", messageId: \""
              + messageId
              + "\", toolCallId: \""
              + rejected
              + "\"}");
      var rejectedChat = stream.awaitChatWithMessages(3);
      assertToolStatus(rejectedChat, rejected, "REJECTED");
      assertThat(rejectedChat.path("messages").get(2).path("status").asString())
          .isEqualTo("PENDING");
    }
  }

  @Test
  void chatUpdated_rollbackOrAnotherChat_emitsNothing() throws Exception {
    var id = startChat().path("id").asString();
    try (var stream = connect(id)) {
      stream.nextChat();
      startChat();
      new TransactionTemplate(transactions)
          .executeWithoutResult(
              tx -> {
                var saved = chats.getById(ChatId.fromString(id));
                saved.completeMessage(saved.getMessages().get(1).getId(), randomString());
                chats.save(saved);
                tx.setRollbackOnly();
              });
      assertThat(stream.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
    }
  }

  @Test
  void websocket_missingInvalidOrWrongAudienceToken_isRejected() {
    for (var token : List.of("", "invalid", TestTokens.forAudience("another-service").mint())) {
      var builder = http.newWebSocketBuilder().subprotocols("graphql-transport-ws");
      if (!token.isEmpty()) builder.header("Authorization", "Bearer " + token);
      assertThatThrownBy(() -> builder.buildAsync(wsUri(), new WebSocket.Listener() {}).join())
          .hasCauseInstanceOf(WebSocketHandshakeException.class);
    }
  }

  private JsonNode startChat() throws Exception {
    return mutate("startChat", "{content: \"" + randomString() + "\"}").path("chat");
  }

  private JsonNode mutate(String name, String input) throws Exception {
    var query =
        "mutation { "
            + name
            + "(input: "
            + input
            + ") { success"
            + (name.equals("approveToolCall") || name.equals("rejectToolCall")
                ? ""
                : " chat { " + FIELDS + " }")
            + " } }";
    var request =
        HttpRequest.newBuilder(URI.create("http://[::1]:" + port + "/graphql"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + TestTokens.forAudience("mops").mint())
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    JSON.writeValueAsString(java.util.Map.of("query", query))))
            .build();
    var response = http.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(200);
    var body = JSON.readTree(response.body());
    assertThat(body.has("errors")).as(response.body()).isFalse();
    assertThat(body.path("data").path(name).path("success").asBoolean()).isTrue();
    return body.path("data").path(name);
  }

  private URI wsUri() {
    return URI.create("ws://[::1]:" + port + "/graphql");
  }

  private Stream connect(String id) throws Exception {
    var stream = new Stream();
    stream.socket =
        http.newWebSocketBuilder()
            .subprotocols("graphql-transport-ws")
            .header("Authorization", "Bearer " + TestTokens.forAudience("mops").mint())
            .buildAsync(wsUri(), stream)
            .get(5, TimeUnit.SECONDS);
    stream.socket.sendText("{\"type\":\"connection_init\"}", true).join();
    assertThat(stream.next().path("type").asString()).isEqualTo("connection_ack");
    stream
        .socket
        .sendText(
            JSON.writeValueAsString(
                java.util.Map.of(
                    "id",
                    "chat",
                    "type",
                    "subscribe",
                    "payload",
                    java.util.Map.of(
                        "query",
                        "subscription { chatUpdated(id: \"" + id + "\") { " + FIELDS + " } }"))),
            true)
        .join();
    return stream;
  }

  private void assertToolStatus(JsonNode chat, ToolCallId id, String status) {
    for (var tool : chat.path("messages").get(1).path("toolCalls")) {
      if (tool.path("id").asString().equals(id.toString())) {
        assertThat(tool.path("status").asString()).isEqualTo(status);
        return;
      }
    }
    throw new AssertionError("Missing tool " + id);
  }

  private static class Stream implements WebSocket.Listener, AutoCloseable {
    final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    final StringBuilder text = new StringBuilder();
    WebSocket socket;

    @Override
    public void onOpen(WebSocket socket) {
      socket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
      text.append(data);
      if (last) {
        messages.add(text.toString());
        text.setLength(0);
      }
      socket.request(1);
      return null;
    }

    JsonNode next() throws Exception {
      var message = messages.poll(5, TimeUnit.SECONDS);
      assertThat(message).as("WebSocket payload").isNotNull();
      return JSON.readTree(message);
    }

    JsonNode nextChat() throws Exception {
      var frame = next();
      assertThat(frame.path("type").asString()).as(frame.toString()).isEqualTo("next");
      assertThat(frame.path("payload").has("errors")).as(frame.toString()).isFalse();
      return frame.path("payload").path("data").path("chatUpdated");
    }

    JsonNode awaitChatWithMessages(int count) throws Exception {
      for (var attempts = 0; attempts < 5; attempts++) {
        var chat = nextChat();
        if (chat.path("messages").size() == count) return chat;
      }
      throw new AssertionError("Expected " + count + " messages");
    }

    @Override
    public void close() {
      socket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
    }
  }
}
