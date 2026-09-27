package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/** Opens the app, follows the redirect to the login form, and submits it like a browser. */
class Browser {
  final WebTestClient client;
  String session;
  String anonymousSession;
  String csrf;
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
    csrf = formToken("/login");
    return client
        .post()
        .uri("/login")
        .cookie("SESSION", session)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(
            BodyInserters.fromFormData("username", GatewayTestSupport.USER.username())
                .with("password", password)
                .with("_csrf", csrf))
        .exchange();
  }

  /**
   * Reads the hidden CSRF field from one of Spring's generated forms. Storing the first token
   * rotates the session, so the browser adopts any cookie the form response sets.
   */
  String formToken(String path) {
    var result =
        client
            .get()
            .uri(path)
            .cookie("SESSION", session)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();
    var rotated = result.getResponseCookies().getFirst("SESSION");
    if (rotated != null) {
      cookie = rotated;
      session = anonymousSession = cookie.getValue();
    }
    var matcher =
        Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(result.getResponseBody());
    assertThat(matcher.find()).as("CSRF field in the generated form").isTrue();
    return matcher.group(1);
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
    csrf = formToken("/logout");
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
