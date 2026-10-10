package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lab.libs.identity.jwt.IdentityClaims;
import lab.libs.identity.jwt.IdentityJwtDecoder;
import lab.libs.identity.testing.TestTokens;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** API clients authenticated by a bearer JWT from the configured issuer. */
class BearerTokenTest extends GatewayTestSupport {

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

  /** The decoder Mops runs, pointed at this gateway's published keys. */
  private JwtDecoder mopsDecoder() {
    return IdentityJwtDecoder.create(
        "http://localhost:" + port + JwkSetController.PATH, "lab-gateway", "mops");
  }

  @Test
  void bearerRequest_withIssuerToken_reachesMopsWithTheIdentityTokenOfASession() {
    var session = mopsDecoder().decode(forwardedToken(browser().login().authenticatedClient()));

    var bearer = mopsDecoder().decode(forwardedToken(bearerClient(bearer(USER).mint())));

    assertThat(bearer.getHeaders()).isEqualTo(session.getHeaders());
    assertThat(bearer.getClaims().keySet()).isEqualTo(session.getClaims().keySet());
    for (var claim : List.of(JwtClaimNames.ISS, JwtClaimNames.AUD)) {
      assertThat(bearer.getClaims().get(claim)).as(claim).isEqualTo(session.getClaims().get(claim));
    }
    assertThat(IdentityClaims.identity(bearer)).isEqualTo(IdentityClaims.identity(session));
    assertThat(session.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly(Caller.FORM);
    assertThat(bearer.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly(Caller.BEARER);
  }

  @Test
  void bearerRequest_forwardsTheGatewayTokenInPlaceOfTheClientToken() {
    var clientToken = bearer(USER).mint();

    var forwarded = forwardedToken(bearerClient(clientToken));

    assertThat(forwarded).isNotEqualTo(clientToken);
    assertThat(mopsDecoder().decode(forwarded).getSubject()).isEqualTo(USER.principal().toString());
  }

  @Test
  void bearerRequest_withUnsafeMethodAndNoCsrfToken_reachesTheService() {
    var before = mopsService.requests();

    bearerClient(bearer(USER).mint())
        .post()
        .uri("/api/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{}")
        .exchange()
        .expectStatus()
        .isOk();

    assertThat(mopsService.requests()).isEqualTo(before + 1);
  }

  @Test
  void bearerRequest_createsNoSession() {
    bearerClient(bearer(USER).mint())
        .get()
        .uri("/api/graphql")
        .exchange()
        .expectStatus()
        .isOk()
        .expectCookie()
        .doesNotExist("SESSION");
  }

  @Test
  void bearerRequest_withExpiredToken_isUnauthorized() {
    assertRejected(bearer(USER).expired().mint());
  }

  @Test
  void bearerRequest_fromAnotherIssuer_isUnauthorized() {
    assertRejected(bearer(USER).issuer(randomString()).mint());
  }

  @Test
  void bearerRequest_forAnotherAudience_isUnauthorized() {
    assertRejected(TestTokens.forAudience(randomString()).issuer(BEARER_ISSUER).mint());
  }

  @Test
  void bearerRequest_signedWithAnotherKey_isUnauthorized() {
    assertRejected(bearer(USER).signedWith(TestTokens.foreignPrivateKey()).mint());
  }

  @Test
  void bearerRequest_withNonUuidTenant_isUnauthorized() {
    assertRejected(bearer(USER).claim(IdentityClaims.TENANT, randomString()).mint());
  }

  @Test
  void bearerRequest_replayingAnIdentityToken_isUnauthorized() {
    var identityToken = forwardedToken(browser().login().authenticatedClient());

    assertRejected(identityToken);
  }

  @Test
  void bearerRequest_withInvalidTokenAlongsideASession_isUnauthorized() {
    var before = mopsService.requests();

    browser()
        .login()
        .authenticatedClient()
        .get()
        .uri("/api/graphql")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomString())
        .exchange()
        .expectStatus()
        .isUnauthorized();

    assertThat(mopsService.requests()).isEqualTo(before);
  }

  /** Rejected even from a browser, since the caller sent a credential rather than none. */
  private void assertRejected(String token) {
    var before = mopsService.requests();

    bearerClient(token)
        .get()
        .uri("/api/graphql")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus()
        .isUnauthorized();

    assertThat(mopsService.requests()).isEqualTo(before);
  }

  private WebTestClient bearerClient(String token) {
    return client().mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
  }

  private static String forwardedToken(WebTestClient client) {
    List<String> forwarded =
        client
            .get()
            .uri("/api/graphql")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class)
            .getResponseHeaders()
            .get(Downstream.AUTHORIZATION);
    assertThat(forwarded).as("one Authorization header reached the service").hasSize(1);
    assertThat(forwarded.get(0)).startsWith("Bearer ");
    return forwarded.get(0).substring("Bearer ".length());
  }
}
