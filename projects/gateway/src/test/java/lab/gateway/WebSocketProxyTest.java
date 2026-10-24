package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;
import lab.libs.identity.Identity;
import lab.libs.identity.jwt.IdentityClaims;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** WebSocket upgrades through the gateway's API route to a stub service. */
class WebSocketProxyTest extends GatewayTestSupport {

  private static final String BEARER = "Bearer ";

  private static final String PATH = "/api/graphql";

  private static WebSocketDownstream mopsService;

  private final HttpClient httpClient = HttpClient.newHttpClient();

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
  void upgrade_withSession_carriesIdentityTokenInPlaceOfClientCredentials()
      throws InterruptedException {
    var browser = browser().login();
    var webSocketBuilder =
        httpClient
            .newWebSocketBuilder()
            .header(HttpHeaders.COOKIE, "SESSION=" + browser.session + "; other=" + randomString())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + randomString());

    var handshake = handshake(webSocketBuilder);

    var jwt = mopsDecoder().decode(bearerToken(handshake.authorization()));
    assertThat(handshake.cookie()).isEmpty();
    assertThat(IdentityClaims.identity(jwt))
        .isEqualTo(new Identity(USER.principal(), USER.tenant(), Set.copyOf(USER.scopes())));
    assertThat(jwt.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly(Caller.FORM);
  }

  @Test
  void upgrade_withBearerToken_carriesIdentityTokenMintedForTheCaller()
      throws InterruptedException {
    var webSocketBuilder =
        httpClient
            .newWebSocketBuilder()
            .header(HttpHeaders.AUTHORIZATION, BEARER + bearer(USER).mint());

    var handshake = handshake(webSocketBuilder);

    var jwt = mopsDecoder().decode(bearerToken(handshake.authorization()));
    assertThat(IdentityClaims.identity(jwt))
        .isEqualTo(new Identity(USER.principal(), USER.tenant(), Set.copyOf(USER.scopes())));
    assertThat(jwt.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly(Caller.BEARER);
  }

  @Test
  void upgrade_withoutSession_isUnauthorizedAndNeverReachesTheService() {
    var requestsBefore = mopsService.requests();

    var thrown =
        catchThrowable(
            () ->
                httpClient
                    .newWebSocketBuilder()
                    .buildAsync(gateway(), new WebSocket.Listener() {})
                    .join());

    assertThat(thrown)
        .isInstanceOf(CompletionException.class)
        .cause()
        .isInstanceOfSatisfying(
            WebSocketHandshakeException.class,
            exception -> assertThat(exception.getResponse().statusCode()).isEqualTo(401));
    assertThat(mopsService.requests()).isEqualTo(requestsBefore);
  }

  /**
   * Opens a socket through the gateway, waits for the service to accept the proxied handshake, and
   * closes the socket. The gateway completes the client's handshake before it connects to the
   * service, so the client's handshake alone does not show that the service was reached.
   */
  private WebSocketDownstream.Handshake handshake(WebSocket.Builder webSocketBuilder)
      throws InterruptedException {
    var webSocket = webSocketBuilder.buildAsync(gateway(), new WebSocket.Listener() {}).join();

    try {
      return mopsService.handshake();
    } finally {
      webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").join();
    }
  }

  /** The IPv6 loopback, which the Bazel sandbox admits where a mapped IPv4 address is refused. */
  private URI gateway() {
    return URI.create("ws://[::1]:" + port + PATH);
  }

  /** The token of the single bearer {@code Authorization} value the service received. */
  private static String bearerToken(List<String> authorization) {
    assertThat(authorization).singleElement().asString().startsWith(BEARER);

    return authorization.get(0).substring(BEARER.length());
  }
}
