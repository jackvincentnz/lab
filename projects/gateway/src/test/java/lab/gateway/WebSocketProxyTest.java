package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lab.libs.identity.Identity;
import lab.libs.identity.jwt.IdentityClaims;
import lab.libs.identity.jwt.IdentityJwtDecoder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** WebSocket upgrades through the gateway to a stub service, connected the way a client would. */
@ExtendWith(OutputCaptureExtension.class)
class WebSocketProxyTest extends GatewayTestSupport {

  private static WebSocketDownstream mopsService;

  private final HttpClient http = HttpClient.newHttpClient();

  @BeforeAll
  static void startDownstream() {
    mopsService = WebSocketDownstream.start();
  }

  @AfterAll
  static void stopDownstream() {
    mopsService.close();
  }

  @DynamicPropertySource
  static void downstreamUris(DynamicPropertyRegistry registry) {
    registry.add("lab.gateway.mops.service-uri", mopsService::uri);
  }

  @Test
  void upgrade_withSession_relaysMessagesBothWays() throws InterruptedException {
    var message = randomString();

    try (var socket = connect(browser().login(), "/api/graphql")) {
      socket.send(message);

      assertThat(socket.next()).isEqualTo("echo " + message);
    }
  }

  @Test
  void upgrade_onApiRoute_reachesServiceWithPrefixStripped() throws InterruptedException {
    try (var socket = connect(browser().login(), "/api/graphql")) {
      assertThat(socket.handshake.path()).isEqualTo("/graphql");
    }
  }

  @Test
  void upgrade_withSession_carriesIdentityTokenInPlaceOfClientCredentials()
      throws InterruptedException {
    var browser = browser().login();
    var client =
        http.newWebSocketBuilder()
            .header(HttpHeaders.COOKIE, "SESSION=" + browser.session + "; other=" + randomString())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomString());

    try (var socket = connect(client, "/api/graphql")) {
      var handshake = socket.handshake;

      assertThat(handshake.cookie()).isEmpty();
      assertThat(handshake.authorization()).hasSize(1);
      assertThat(handshake.authorization().get(0)).startsWith("Bearer ");
      var jwt =
          mopsDecoder().decode(handshake.authorization().get(0).substring("Bearer ".length()));
      assertThat(IdentityClaims.identity(jwt))
          .isEqualTo(new Identity(USER.principal(), USER.tenant(), Set.copyOf(USER.scopes())));
      assertThat(jwt.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
          .containsExactly(Caller.FORM);
    }
  }

  @Test
  void upgrade_withSubprotocol_negotiatesItWithClientAndService() throws InterruptedException {
    try (var socket = connect(browser().login(), "/api/graphql")) {
      assertThat(socket.webSocket.getSubprotocol()).isEqualTo(WebSocketDownstream.SUBPROTOCOL);
      assertThat(socket.handshake.subprotocol()).isEqualTo(WebSocketDownstream.SUBPROTOCOL);
    }
  }

  @Test
  void upgrade_withSession_sendsTheServiceAsHost() throws InterruptedException {
    try (var socket = connect(browser().login(), "/api/graphql")) {
      assertThat(socket.handshake.host()).endsWith(":" + mopsService.port());
    }
  }

  @Test
  void upgrade_withoutSession_isUnauthorizedAndNeverReachesTheService() {
    var before = mopsService.requests();

    assertThatThrownBy(
            () ->
                http.newWebSocketBuilder()
                    .subprotocols(WebSocketDownstream.SUBPROTOCOL)
                    .buildAsync(gateway("/api/graphql"), new WebSocket.Listener() {})
                    .join())
        .isInstanceOf(CompletionException.class)
        .cause()
        .isInstanceOfSatisfying(
            WebSocketHandshakeException.class,
            exception -> assertThat(exception.getResponse().statusCode()).isEqualTo(401));
    assertThat(mopsService.requests()).isEqualTo(before);
  }

  @Test
  void upgrade_withSession_logsAccessRecord(CapturedOutput output) throws InterruptedException {
    var path = "/api/" + randomString();

    try (var socket = connect(browser().login(), path)) {
      var requestId = socket.handshake.requestId();

      // The record is written as WebFlux commits the response, before the server sends the 101.
      assertThat(record(output, requestId))
          .containsEntry("message", "Access")
          .containsEntry("principal_id", USER.principal().toString())
          .containsEntry("path", path)
          .containsEntry("status", 200)
          .containsEntry("request_id", requestId);
    }
  }

  /** The decoder Mops runs, pointed at this gateway's published keys. */
  private JwtDecoder mopsDecoder() {
    return IdentityJwtDecoder.create(
        "http://localhost:" + port + JwkSetController.PATH, "lab-gateway", "mops");
  }

  /** The IPv6 loopback, which the Bazel sandbox admits where a mapped IPv4 address is refused. */
  private URI gateway(String path) {
    return URI.create("ws://[::1]:" + port + path);
  }

  private Socket connect(Browser browser, String path) throws InterruptedException {
    return connect(
        http.newWebSocketBuilder().header(HttpHeaders.COOKIE, "SESSION=" + browser.session), path);
  }

  /**
   * Opens a socket through the gateway and waits for the service to accept the proxied handshake,
   * since the gateway completes the client's handshake before it connects to the service.
   */
  private Socket connect(WebSocket.Builder client, String path) throws InterruptedException {
    var socket = new Socket();
    socket.webSocket =
        client
            .subprotocols(WebSocketDownstream.SUBPROTOCOL)
            .buildAsync(gateway(path), socket)
            .join();
    socket.handshake = mopsService.handshake();
    return socket;
  }

  /** The console line carrying {@code requestId}, parsed. */
  private static Map<String, Object> record(CapturedOutput output, String requestId) {
    var lines =
        output
            .getOut()
            .lines()
            .filter(line -> line.startsWith("{") && line.contains(requestId))
            .toList();
    assertThat(lines).hasSize(1);
    return JsonPath.parse(lines.get(0)).read("$");
  }

  /** A client socket that queues the text messages the gateway relays to it. */
  private static final class Socket implements WebSocket.Listener, AutoCloseable {
    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    private final StringBuilder partial = new StringBuilder();
    WebSocket webSocket;
    WebSocketDownstream.Handshake handshake;

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      partial.append(data);
      if (last) {
        messages.add(partial.toString());
        partial.setLength(0);
      }
      webSocket.request(1);
      return null;
    }

    void send(String message) {
      webSocket.sendText(message, true).join();
    }

    String next() throws InterruptedException {
      var message = messages.poll(10, TimeUnit.SECONDS);
      assertThat(message).as("message relayed by the gateway").isNotNull();
      return message;
    }

    @Override
    public void close() {
      webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").join();
    }
  }
}
