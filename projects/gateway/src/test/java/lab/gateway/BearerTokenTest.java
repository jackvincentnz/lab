package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import lab.libs.identity.Identity;
import lab.libs.identity.jwt.IdentityClaims;
import lab.libs.identity.testing.TestTokens;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

class BearerTokenTest extends GatewayTestSupport {

  private static Downstream mopsService;

  @BeforeAll
  static void startDownstream() {
    mopsService = Downstream.start("service");
  }

  @AfterAll
  static void stopDownstream() {
    mopsService.close();
  }

  @DynamicPropertySource
  static void downstreamUris(DynamicPropertyRegistry registry) {
    registry.add("lab.gateway.mops.service-uri", mopsService::uri);
  }

  @Test
  void bearerRequest_withIssuerToken_forwardsAnIdentityTokenForTheCaller() {
    var token = bearer(USER).mint();

    var jwt = mopsDecoder().decode(forwardedToken(bearerClient(token)));

    assertThat(IdentityClaims.identity(jwt))
        .isEqualTo(new Identity(USER.principal(), USER.tenant(), Set.copyOf(USER.scopes())));
    assertThat(jwt.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly(Caller.BEARER);
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
  void bearerRequest_withIssuerToken_createsNoSession() {
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
    assertUnauthorizedNotRedirected(bearer(USER).expired().mint());
  }

  @Test
  void bearerRequest_fromAnotherIssuer_isUnauthorized() {
    assertUnauthorizedNotRedirected(bearer(USER).issuer(randomString()).mint());
  }

  @Test
  void bearerRequest_forAnotherAudience_isUnauthorized() {
    assertUnauthorizedNotRedirected(
        TestTokens.forAudience(randomString()).issuer(BEARER_ISSUER).mint());
  }

  @Test
  void bearerRequest_signedWithAnotherKey_isUnauthorized() {
    assertUnauthorizedNotRedirected(bearer(USER).signedWith(TestTokens.foreignPrivateKey()).mint());
  }

  @Test
  void bearerRequest_withNonUuidTenant_isUnauthorized() {
    assertUnauthorizedNotRedirected(
        bearer(USER).claim(IdentityClaims.TENANT, randomString()).mint());
  }

  @Test
  void bearerRequest_replayingAnIdentityToken_isUnauthorized() {
    var identityToken = forwardedToken(browser().login().authenticatedClient());

    assertUnauthorizedNotRedirected(identityToken);
  }

  @Test
  void bearerRequest_withValidTokenAndSession_isBadRequest() {
    assertBadRequestWithSession(bearer(USER).mint());
  }

  @Test
  void bearerRequest_withInvalidTokenAndSession_isBadRequest() {
    assertBadRequestWithSession(randomString());
  }

  @Test
  void bearerRequest_withUnknownSessionCookie_reachesTheService() {
    var before = mopsService.requests();

    bearerClient(bearer(USER).mint())
        .get()
        .uri("/api/graphql")
        .cookie("SESSION", randomString())
        .exchange()
        .expectStatus()
        .isOk();

    assertThat(mopsService.requests()).isEqualTo(before + 1);
  }

  private static String forwardedToken(WebTestClient webTestClient) {
    List<String> forwarded =
        webTestClient
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

  private WebTestClient bearerClient(String token) {
    return client().mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
  }

  private void assertBadRequestWithSession(String token) {
    var before = mopsService.requests();

    browser()
        .login()
        .authenticatedClient()
        .get()
        .uri("/api/graphql")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .value(
            HttpHeaders.WWW_AUTHENTICATE,
            value -> assertThat(value).contains("error=\"invalid_request\""));

    assertThat(mopsService.requests()).isEqualTo(before);
  }

  private void assertUnauthorizedNotRedirected(String token) {
    var before = mopsService.requests();

    bearerClient(token)
        .get()
        .uri("/api/graphql")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectHeader()
        .value(
            HttpHeaders.WWW_AUTHENTICATE,
            value -> assertThat(value).startsWith("Bearer").contains("error=\"invalid_token\""));

    assertThat(mopsService.requests()).isEqualTo(before);
  }
}
