package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lab.libs.identity.jwt.IdentityClaims;
import lab.libs.identity.testing.TestTokens;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

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

  @Test
  void bearerRequest_withIssuerToken_reachesMopsWithTheIdentityTokenOfASession() {
    var sessionJwt = mopsDecoder().decode(forwardedToken(browser().login().authenticatedClient()));

    var bearerJwt = mopsDecoder().decode(forwardedToken(bearerClient(bearer(USER).mint())));

    assertThat(bearerJwt.getHeaders()).isEqualTo(sessionJwt.getHeaders());
    assertThat(bearerJwt.getClaims().keySet()).isEqualTo(sessionJwt.getClaims().keySet());
    for (var claim : List.of(JwtClaimNames.ISS, JwtClaimNames.AUD)) {
      assertThat(bearerJwt.getClaims().get(claim))
          .as(claim)
          .isEqualTo(sessionJwt.getClaims().get(claim));
    }

    assertThat(IdentityClaims.identity(bearerJwt)).isEqualTo(IdentityClaims.identity(sessionJwt));
    assertThat(sessionJwt.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly(Caller.FORM);
    assertThat(bearerJwt.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly(Caller.BEARER);
  }

  @Test
  void bearerRequest_withIssuerToken_forwardsTheGatewayTokenInPlaceOfTheClientToken() {
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
