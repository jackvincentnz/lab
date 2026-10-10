package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import lab.libs.identity.Identity;
import lab.libs.identity.jwt.IdentityClaims;
import lab.libs.identity.jwt.IdentityJwtDecoder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * What a downstream service receives in place of the browser's credentials, verified as Mops would.
 */
class IdentityContractTest extends GatewayTestSupport {

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
  void jwkSet_isPublic() {
    client()
        .get()
        .uri(JwkSetController.PATH)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.keys[0].kid")
        .isNotEmpty()
        .jsonPath("$.keys[0].d")
        .doesNotExist();
  }

  @Test
  void serviceRequest_carriesTokenMopsAcceptsAgainstThePublishedKeys() {
    var browser = browser().login();

    var jwt = mopsDecoder().decode(forwardedToken(browser, "/api/graphql"));

    assertThat(IdentityClaims.identity(jwt))
        .isEqualTo(new Identity(USER.principal(), USER.tenant(), Set.copyOf(USER.scopes())));
    assertThat(jwt.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly("form");
  }

  @Test
  void serviceRequest_replacesEveryClientAuthorizationHeader() {
    var browser = browser().login();

    var forwarded =
        browser
            .authenticatedClient()
            .get()
            .uri("/api/graphql")
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + bearer(USER).mint(),
                "Basic " + randomString())
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class)
            .getResponseHeaders()
            .get(Downstream.AUTHORIZATION);

    assertThat(forwarded).hasSize(1);
    Jwt jwt = mopsDecoder().decode(bearer(forwarded.get(0)));
    assertThat(jwt.getSubject()).isEqualTo(USER.principal().toString());
  }

  @Test
  void bearerRequest_withoutSession_neverReachesTheService() {
    var before = mopsService.requests();

    client()
        .get()
        .uri("/api/graphql")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomString())
        .exchange()
        .expectStatus()
        .isUnauthorized();

    assertThat(mopsService.requests()).isEqualTo(before);
  }

  @Test
  void appRequest_forwardsNoCredentials() {
    browser()
        .login()
        .authenticatedClient()
        .get()
        .uri("/spend")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer(USER).mint())
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .doesNotExist(Downstream.AUTHORIZATION);
  }

  private String forwardedToken(Browser browser, String path) {
    List<String> forwarded =
        browser
            .authenticatedClient()
            .get()
            .uri(path)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class)
            .getResponseHeaders()
            .get(Downstream.AUTHORIZATION);
    assertThat(forwarded).as("one Authorization header reached the service").hasSize(1);
    return bearer(forwarded.get(0));
  }

  private static String bearer(String header) {
    assertThat(header).startsWith("Bearer ");
    return header.substring("Bearer ".length());
  }
}
