package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lab.libs.identity.jwt.IdentityClaims;
import lab.libs.identity.jwt.IdentityJwtDecoder;
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

/** API clients authenticated by a bearer JWT signed with the static client key. */
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
  void bearerRequest_withLocallyMintedToken_reachesMopsWithTheIdentityTokenOfASession() {
    var session = mopsDecoder().decode(forwardedToken(browser().login().authenticatedClient()));

    var bearer =
        mopsDecoder()
            .decode(
                forwardedToken(bearerClient(signBearer(bearerClaims(USER).build(), BEARER_KEY))));

    assertThat(bearer.getHeaders()).isEqualTo(session.getHeaders());
    assertThat(bearer.getClaims().keySet()).isEqualTo(session.getClaims().keySet());
    for (var claim :
        List.of(
            JwtClaimNames.ISS,
            JwtClaimNames.AUD,
            JwtClaimNames.SUB,
            IdentityClaims.TENANT,
            IdentityClaims.SCOPE)) {
      assertThat(bearer.getClaims().get(claim)).as(claim).isEqualTo(session.getClaims().get(claim));
    }
    assertThat(session.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly(Caller.FORM);
    assertThat(bearer.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly(Caller.BEARER);
  }

  @Test
  void bearerRequest_forwardsTheGatewayTokenInPlaceOfTheClientToken() {
    var clientToken = signBearer(bearerClaims(USER).build(), BEARER_KEY);

    var forwarded = forwardedToken(bearerClient(clientToken));

    assertThat(forwarded).isNotEqualTo(clientToken);
    assertThat(mopsDecoder().decode(forwarded).getSubject()).isEqualTo(USER.principal().toString());
  }

  @Test
  void bearerRequest_withUnsafeMethodAndNoCsrfToken_reachesTheService() {
    var before = mopsService.requests();

    bearerClient(signBearer(bearerClaims(USER).build(), BEARER_KEY))
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
    bearerClient(signBearer(bearerClaims(USER).build(), BEARER_KEY))
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
    var issuedAt = Instant.now().minus(Duration.ofHours(1));

    assertRejected(
        signBearer(
            bearerClaims(USER)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(Duration.ofMinutes(5)))
                .build(),
            BEARER_KEY));
  }

  @Test
  void bearerRequest_fromAnotherIssuer_isUnauthorized() {
    assertRejected(signBearer(bearerClaims(USER).issuer(randomString()).build(), BEARER_KEY));
  }

  @Test
  void bearerRequest_forAnotherAudience_isUnauthorized() {
    assertRejected(
        signBearer(bearerClaims(USER).audience(List.of(randomString())).build(), BEARER_KEY));
  }

  @Test
  void bearerRequest_signedWithAnotherKey_isUnauthorized() {
    assertRejected(signBearer(bearerClaims(USER).build(), keyPair()));
  }

  @Test
  void bearerRequest_withNonUuidTenant_isUnauthorized() {
    assertRejected(
        signBearer(
            bearerClaims(USER).claim(IdentityClaims.TENANT, randomString()).build(), BEARER_KEY));
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
