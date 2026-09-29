package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/** Routes Mops traffic to stub downstreams that echo which server and path they received. */
class MopsRoutingTest extends GatewayTestSupport {

  private static DisposableServer mopsService;
  private static DisposableServer mopsApp;

  @BeforeAll
  static void startDownstreams() {
    mopsService = echoServer("service");
    mopsApp = echoServer("app");
  }

  @AfterAll
  static void stopDownstreams() {
    mopsService.disposeNow();
    mopsApp.disposeNow();
  }

  @DynamicPropertySource
  static void downstreamUris(DynamicPropertyRegistry registry) {
    registry.add("lab.gateway.mops.service-uri", () -> "http://localhost:" + mopsService.port());
    registry.add("lab.gateway.mops.app-uri", () -> "http://localhost:" + mopsApp.port());
  }

  private static DisposableServer echoServer(String name) {
    return HttpServer.create()
        .port(0)
        .handle(
            (request, response) -> {
              request
                  .requestHeaders()
                  .getAll("Cookie")
                  .forEach(cookie -> response.addHeader("X-Downstream-Cookie", cookie));
              request
                  .requestHeaders()
                  .getAll("Authorization")
                  .forEach(value -> response.addHeader("X-Downstream-Authorization", value));
              return response.sendString(Mono.just(name + " " + request.uri()));
            })
        .bindNow();
  }

  private WebTestClient authenticatedClient() {
    return browser().login().authenticatedClient();
  }

  @Test
  void apiRequest_routesToServiceWithPrefixStripped() {
    authenticatedClient()
        .post()
        .uri("/api/graphql")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("service /graphql");
  }

  @Test
  void bareApiPath_routesToServiceRoot() {
    authenticatedClient()
        .get()
        .uri("/api")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("service /");
  }

  @Test
  void pageRequest_routesToApp() {
    authenticatedClient()
        .get()
        .uri("/spend")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("app /spend");
  }

  @Test
  void assetRequest_routesToApp() {
    authenticatedClient()
        .get()
        .uri("/src/main.js")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("app /src/main.js");
  }

  @Test
  void serviceRequest_dropsBrowserCookies() {
    authenticatedClient()
        .get()
        .uri("/api/cookie-check")
        .cookie("other", "browser-value")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .doesNotExist("X-Downstream-Cookie")
        .expectBody(String.class)
        .isEqualTo("service /cookie-check");
  }

  @Test
  void appRequest_dropsBrowserCookies() {
    authenticatedClient()
        .get()
        .uri("/cookie-check")
        .cookie("other", "browser-value")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .doesNotExist("X-Downstream-Cookie")
        .expectBody(String.class)
        .isEqualTo("app /cookie-check");
  }

  @Test
  void forwardedRequests_replaceAllClientAuthorizationWithSignedIdentity() throws Exception {
    var browser = browser().login();
    var keys =
        client()
            .get()
            .uri("/.well-known/jwks.json")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();
    var jwks = JWKSet.parse(keys);
    assertThat(jwks.getKeys()).allMatch(key -> !key.isPrivate());
    String previousId = null;
    for (var path : new String[] {"/api/identity", "/spend"}) {
      var result =
          browser
              .authenticatedClient()
              .get()
              .uri(path)
              .header("Authorization", "Bearer attacker", "Basic attacker")
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(String.class)
              .returnResult();
      var headers = result.getResponseHeaders().get("X-Downstream-Authorization");
      assertThat(headers).hasSize(1);
      var token = SignedJWT.parse(headers.get(0).substring("Bearer ".length()));
      assertThat(
              token.verify(
                  new RSASSAVerifier(jwks.getKeyByKeyId(token.getHeader().getKeyID()).toRSAKey())))
          .isTrue();
      assertThat(token.getJWTClaimsSet().getSubject()).isEqualTo(USER.principal().toString());
      assertThat(token.getJWTClaimsSet().getStringClaim("sid")).isEqualTo(browser.session);
      assertThat(token.getJWTClaimsSet().getJWTID()).isNotEqualTo(previousId);
      previousId = token.getJWTClaimsSet().getJWTID();
    }
  }

  @Test
  void bearerWithoutSession_cannotReachDownstream() {
    client()
        .get()
        .uri("/api/identity")
        .header("Authorization", "Bearer attacker")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void servesHealthFromTheGateway() {
    client()
        .get()
        .uri("/actuator/health")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo("UP");
  }
}
