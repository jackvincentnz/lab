package lab.gateway;

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
