package lab.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class UnauthenticatedRequestTest extends GatewayTestSupport {

  @Test
  void browserRequest_withoutSession_redirectsToLogin() {
    client()
        .get()
        .uri("/spend")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus()
        .isFound()
        .expectHeader()
        .valueEquals("Location", "/login");
  }

  @Test
  void apiRequest_withoutSession_isUnauthorized() {
    client()
        .get()
        .uri("/api/graphql")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectHeader()
        .valueEquals(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
  }

  @Test
  void apiRequest_withBasicAuthorizationAndNoSession_isUnauthorized() {
    client()
        .get()
        .uri("/api/graphql")
        .header(HttpHeaders.AUTHORIZATION, "Basic " + randomString())
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectHeader()
        .valueEquals(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
  }

  @Test
  void request_withoutAcceptHeader_isUnauthorized() {
    client().get().uri("/api/graphql").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void apiPost_withoutCredentialsOrCsrfToken_isForbidden() {
    client()
        .post()
        .uri("/api/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"query\":\"" + randomString() + "\"}")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void browserRequest_withUnknownSessionCookie_redirectsToLogin() {
    client()
        .get()
        .uri("/")
        .accept(MediaType.TEXT_HTML)
        .cookie("SESSION", randomString())
        .exchange()
        .expectStatus()
        .isFound()
        .expectHeader()
        .valueEquals("Location", "/login");
  }
}
