package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.web.reactive.function.BodyInserters;

class FormLoginTest extends GatewayTestSupport {
  @Autowired ReactiveSessionRepository<? extends Session> sessions;

  @Test
  void redirectsAnonymousBrowserToLogin() {
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
  void rejectsAnonymousApiRequestWithUnauthorized() {
    client()
        .get()
        .uri("/api/csrf")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized();
    client().get().uri("/api/csrf").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void rotatesSessionAndStoresIdentity() {
    var browser = browser().login();
    assertThat(browser.session).isNotEqualTo(browser.anonymousSession);
    assertThat(browser.cookie.isHttpOnly()).isTrue();
    assertThat(browser.cookie.isSecure()).isTrue();
    assertThat(browser.cookie.getSameSite()).isEqualTo("Lax");
    assertThat(browser.cookie.getPath()).isEqualTo("/");
    assertThat(sessions.findById(browser.anonymousSession).block()).isNull();
    Session session = sessions.findById(browser.session).block();
    assertThat(session).isNotNull();
    assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(30));
    SecurityContext context = session.getAttribute("SPRING_SECURITY_CONTEXT");
    assertThat(context.getAuthentication().isAuthenticated()).isTrue();
    var principal = (GatewayPrincipal) context.getAuthentication().getPrincipal();
    assertThat(principal.principal()).isEqualTo(UUID.fromString(PRINCIPAL));
    assertThat(principal.tenant()).isEqualTo(UUID.fromString(TENANT));
    assertThat(principal.scopes()).containsExactly("mops:read", "mops:write");
    assertThat(principal.getPassword()).isNull();
    assertThat(context.getAuthentication().getCredentials()).isNull();
    // A second login must work after the first authentication erased its credentials.
    browser().login();
  }

  @Test
  void rejectsBadPassword() {
    var browser = browser();
    browser
        .login("wrong")
        .expectStatus()
        .isFound()
        .expectHeader()
        .valueEquals("Location", "/login?error");
    Session session = sessions.findById(browser.session).block();
    assertThat((Object) session.getAttribute("SPRING_SECURITY_CONTEXT")).isNull();
  }

  @Test
  void requiresCsrfForUnsafeRequests() {
    client()
        .post()
        .uri("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData("username", "alice").with("password", "password"))
        .exchange()
        .expectStatus()
        .isForbidden();
    var browser = browser().login();
    client()
        .post()
        .uri("/logout")
        .cookie("SESSION", browser.session)
        .exchange()
        .expectStatus()
        .isForbidden();
    assertThat(sessions.findById(browser.session).block()).isNotNull();
    client()
        .post()
        .uri("/api/graphql")
        .cookie("SESSION", browser.session)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void servesSessionCsrfToken() {
    var browser = browser().login();
    var token =
        client()
            .get()
            .uri("/api/csrf")
            .cookie("SESSION", browser.session)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .valueMatches("Cache-Control", ".*no-store.*")
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();
    assertThat(token).containsEntry("headerName", "X-CSRF-TOKEN");
    client()
        .post()
        .uri("/logout")
        .cookie("SESSION", browser.session)
        .header("X-CSRF-TOKEN", (String) token.get("token"))
        .exchange()
        .expectStatus()
        .isFound()
        .expectHeader()
        .valueEquals("Location", "/login?logout");
  }

  @Test
  void logoutInvalidatesSessionAndRejectsReplay() {
    var browser = browser().login();
    browser
        .authenticatedClient()
        .post()
        .uri("/logout")
        .exchange()
        .expectStatus()
        .isFound()
        .expectHeader()
        .valueEquals("Location", "/login?logout")
        .expectCookie()
        .maxAge("SESSION", Duration.ZERO);
    assertThat(sessions.findById(browser.session).block()).isNull();
    client()
        .get()
        .uri("/spend")
        .cookie("SESSION", browser.session)
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus()
        .isFound()
        .expectHeader()
        .valueEquals("Location", "/login");
  }
}
