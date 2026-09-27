package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/** Drives the generated login and logout forms the way a browser would. */
class Browser {
  final WebTestClient client;
  String session;
  String csrf;
  String anonymousSession;
  ResponseCookie cookie;

  Browser(WebTestClient client) {
    this.client = client;
  }

  void form(String path) {
    var request = client.get().uri(path);
    if (session != null) request.cookie("SESSION", session);
    var response = request.exchange().expectStatus().isOk().expectBody(String.class).returnResult();
    var nextCookie = response.getResponseCookies().getFirst("SESSION");
    if (nextCookie != null) {
      cookie = nextCookie;
      session = cookie.getValue();
    }
    var matcher =
        Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"")
            .matcher(response.getResponseBody());
    assertThat(matcher.find()).as("CSRF field in the generated form").isTrue();
    csrf = matcher.group(1);
  }

  WebTestClient.ResponseSpec login(String password) {
    form("/login");
    anonymousSession = session;
    return client
        .post()
        .uri("/login")
        .cookie("SESSION", session)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(
            BodyInserters.fromFormData("username", "alice")
                .with("password", password)
                .with("_csrf", csrf))
        .exchange();
  }

  Browser login() {
    var response =
        login("password")
            .expectStatus()
            .isFound()
            .expectHeader()
            .valueEquals("Location", "/")
            .expectBody()
            .returnResult();
    cookie = response.getResponseCookies().getFirst("SESSION");
    assertThat(cookie).isNotNull();
    session = cookie.getValue();
    form("/logout");
    return this;
  }

  WebTestClient authenticatedClient() {
    return client
        .mutate()
        .defaultCookie("SESSION", session)
        .defaultHeader("X-CSRF-TOKEN", csrf)
        .build();
  }
}
