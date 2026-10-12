package lab.mops.ai.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lab.mops.ai.application.chat.ToolProvider;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.CompletionService;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

/** Keeps the existing async completion handler real, replacing only the model boundary. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "server.address=::1",
      "spring.ai.google.genai.api-key=synthetic-test-key",
      "mops.identity.development.enabled=true"
    })
class AsyncChatSubscriptionTest extends TestBase {
  @LocalServerPort int port;

  @MockitoBean(name = "springChatClient", extraInterfaces = ToolProvider.class)
  CompletionService completions;

  @Test
  void chatUpdated_asyncCompletion_arrivesWithoutAnotherHttpRequest() throws Exception {
    var completed = randomString();
    var release = new CountDownLatch(1);
    var entered = new CountDownLatch(1);
    when(((ToolProvider) completions).getTools()).thenReturn(List.of());
    when(completions.getResponse(anyList()))
        .thenAnswer(
            ignored -> {
              entered.countDown();
              assertThat(release.await(5, TimeUnit.SECONDS)).isTrue();
              return AssistantMessage.of(completed);
            });
    var http = HttpClient.newHttpClient();
    var json = new ObjectMapper();
    var request =
        HttpRequest.newBuilder(URI.create("http://[::1]:" + port + "/graphql"))
            .header("Content-Type", "application/json")
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    json.writeValueAsString(
                        Map.of(
                            "query",
                            "mutation { startChat(input: {content: \""
                                + randomString()
                                + "\"}) { chat { id } } }"))))
            .build();
    var response = json.readTree(http.send(request, HttpResponse.BodyHandlers.ofString()).body());
    assertThat(response.has("errors")).as(response.toString()).isFalse();
    var id = response.path("data").path("startChat").path("chat").path("id").asString();
    assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
    var frames = new LinkedBlockingQueue<String>();
    var socket =
        http.newWebSocketBuilder()
            .subprotocols("graphql-transport-ws")
            .buildAsync(
                URI.create("ws://[::1]:" + port + "/graphql"),
                new WebSocket.Listener() {
                  private final StringBuilder text = new StringBuilder();

                  @Override
                  public void onOpen(WebSocket socket) {
                    socket.request(1);
                  }

                  @Override
                  public CompletionStage<?> onText(
                      WebSocket socket, CharSequence part, boolean last) {
                    text.append(part);
                    if (last) {
                      frames.add(text.toString());
                      text.setLength(0);
                    }
                    socket.request(1);
                    return null;
                  }
                })
            .get(5, TimeUnit.SECONDS);
    try {
      socket.sendText("{\"type\":\"connection_init\"}", true).join();
      assertThat(json.readTree(frames.poll(5, TimeUnit.SECONDS)).path("type").asString())
          .isEqualTo("connection_ack");
      socket
          .sendText(
              json.writeValueAsString(
                  Map.of(
                      "id",
                      "chat",
                      "type",
                      "subscribe",
                      "payload",
                      Map.of(
                          "query",
                          "subscription { chatUpdated(id: \""
                              + id
                              + "\") { messages { status content } } }"))),
              true)
          .join();
      var initial = json.readTree(frames.poll(5, TimeUnit.SECONDS));
      assertThat(
              initial
                  .path("payload")
                  .path("data")
                  .path("chatUpdated")
                  .path("messages")
                  .get(1)
                  .path("status")
                  .asString())
          .isEqualTo("PENDING");
      release.countDown();
      var update = json.readTree(frames.poll(5, TimeUnit.SECONDS));
      var assistant =
          update.path("payload").path("data").path("chatUpdated").path("messages").get(1);
      assertThat(assistant.path("status").asString()).as(update.toString()).isEqualTo("COMPLETED");
      assertThat(assistant.path("content").asString()).isEqualTo(completed);
    } finally {
      release.countDown();
      socket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
    }
  }
}
