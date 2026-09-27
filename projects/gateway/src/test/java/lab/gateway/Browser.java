package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/** Opens the app, follows the redirect to the login form, and submits it like a browser. */
class Browser {
  final WebTestClient client;
  String session;
  String anonymousSession;
  ResponseCookie cookie;

  Browser(WebTestClient client) {
    this.client = client;
  }

  WebTestClient.ResponseSpec login(String password) {
    cookie =
        client
            .get()
            .uri("/")
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus()
            .isFound()
            .returnResult(String.class)
            .getResponseCookies()
            .getFirst("SESSION");
    assertThat(cookie).as("session created for the saved request").isNotNull();
    session = anonymousSession = cookie.getValue();
    return client
        .post()
        .uri("/login")
        .cookie("SESSION", session)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(
            BodyInserters.fromFormData("username", GatewayTestSupport.USER.username())
                .with("password", password))
        .exchange();
  }

  Browser login() {
    var response =
        login(GatewayTestSupport.PASSWORD)
            .expectStatus()
            .isFound()
            .expectHeader()
            .valueEquals("Location", "/")
            .expectBody()
            .returnResult();
    cookie = response.getResponseCookies().getFirst("SESSION");
    assertThat(cookie).isNotNull();
    session = cookie.getValue();
    return this;
  }

  WebTestClient authenticatedClient() {
    return client.mutate().defaultCookie("SESSION", session).build();
  }
}
