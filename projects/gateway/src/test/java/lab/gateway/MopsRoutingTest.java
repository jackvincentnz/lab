package lab.gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Routes Mops traffic to stub downstreams that echo which server and path they received. */
class MopsRoutingTest extends GatewayTestSupport {

  private static Downstream mopsService;
  private static Downstream mopsApp;

  @BeforeAll
  static void startDownstreams() {
    mopsService = Downstream.start("service");
    mopsApp = Downstream.start("app");
  }

  @AfterAll
  static void stopDownstreams() {
    mopsService.close();
    mopsApp.close();
  }

  @DynamicPropertySource
  static void downstreamUris(DynamicPropertyRegistry registry) {
    registry.add("lab.gateway.mops.service-uri", mopsService::uri);
    registry.add("lab.gateway.mops.app-uri", mopsApp::uri);
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
        .doesNotExist(Downstream.COOKIE)
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
        .doesNotExist(Downstream.COOKIE)
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
