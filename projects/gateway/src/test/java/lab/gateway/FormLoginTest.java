package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;

class FormLoginTest extends GatewayTestSupport {

  @Autowired ReactiveSessionRepository<? extends Session> sessions;

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
        .isUnauthorized();
  }

  @Test
  void request_withoutAcceptHeader_isUnauthorized() {
    client().get().uri("/api/graphql").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void login_rotatesSessionId() {
    var browser = browser().login();

    assertThat(browser.session).isNotEqualTo(browser.anonymousSession);
    assertThat(sessions.findById(browser.anonymousSession).block()).isNull();
    assertThat(sessions.findById(browser.session).block()).isNotNull();
  }

  @Test
  void login_setsCookieAttributes() {
    var browser = browser().login();

    assertThat(browser.cookie.isHttpOnly()).isTrue();
    assertThat(browser.cookie.isSecure()).isTrue();
    assertThat(browser.cookie.getSameSite()).isEqualTo("Lax");
    assertThat(browser.cookie.getPath()).isEqualTo("/");
  }

  @Test
  void login_storesIdentityWithoutCredentials() {
    var browser = browser().login();

    Session session = sessions.findById(browser.session).block();
    assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(30));
    SecurityContext context = session.getAttribute("SPRING_SECURITY_CONTEXT");
    var principal = (GatewayPrincipal) context.getAuthentication().getPrincipal();
    assertThat(principal.principal()).isEqualTo(USER.principal());
    assertThat(principal.tenant()).isEqualTo(USER.tenant());
    assertThat(principal.scopes()).isEqualTo(USER.scopes());
    assertThat(principal.getPassword()).isNull();
    assertThat(context.getAuthentication().getCredentials()).isNull();
  }

  @Test
  void login_succeedsAgainAfterCredentialsWereErased() {
    browser().login();

    browser().login();
  }

  @Test
  void login_withWrongPassword_redirectsToErrorAndKeepsSessionAnonymous() {
    var browser = browser();

    browser
        .login(randomString())
        .expectStatus()
        .isFound()
        .expectHeader()
        .valueEquals("Location", "/login?error");

    Session session = sessions.findById(browser.session).block();
    assertThat((Object) session.getAttribute("SPRING_SECURITY_CONTEXT")).isNull();
  }

  @Test
  void logout_deletesSessionAndExpiresCookie() {
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
  }

  @Test
  void logout_rejectsReplayedCookie() {
    var browser = browser().login();
    browser.authenticatedClient().post().uri("/logout").exchange().expectStatus().isFound();

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
