package lab.mops.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lab.libs.identity.testing.JwkSetServer;
import lab.libs.identity.testing.TestTokens;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Mops in front of callers: every non-public request must carry a valid identity token. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(IdentityProbe.class)
class IdentityContractTest {

  static final String QUERY = "{\"query\":\"{ allBudgets { id } }\"}";

  private static JwkSetServer jwkSet;

  @Autowired MockMvcTester mvc;

  @BeforeAll
  static void publishKeys() {
    jwkSet = JwkSetServer.start(TestTokens.devJwkSet());
  }

  @AfterAll
  static void unpublishKeys() {
    jwkSet.close();
  }

  @DynamicPropertySource
  static void jwkSetUri(DynamicPropertyRegistry registry) {
    registry.add("mops.identity.jwk-set-uri", () -> jwkSet.uri());
  }

  @Test
  void health_isPublic() {
    assertThat(mvc.get().uri("/actuator/health")).hasStatusOk();
  }

  @Test
  void request_withoutToken_isRejectedBeforeDispatch() {
    assertThat(mvc.post().uri("/graphql").contentType(MediaType.APPLICATION_JSON).content(QUERY))
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .headers()
        .containsHeader(HttpHeaders.WWW_AUTHENTICATE);
  }

  @Test
  void request_withTokenSignedByAnotherKey_isRejected() {
    var token = TestTokens.forAudience("mops").signedWith(TestTokens.foreignPrivateKey()).mint();

    assertThat(mvc.get().uri("/identity-probe").header(HttpHeaders.AUTHORIZATION, bearer(token)))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void request_withTokenNamingAnUnpublishedKey_isRejected() {
    var token = TestTokens.forAudience("mops").keyId("retired").mint();

    assertThat(mvc.get().uri("/identity-probe").header(HttpHeaders.AUTHORIZATION, bearer(token)))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void request_withTokenWithoutExpiry_isRejected() {
    var token = TestTokens.forAudience("mops").withoutExpiry().mint();

    assertThat(mvc.get().uri("/identity-probe").header(HttpHeaders.AUTHORIZATION, bearer(token)))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void request_withTokenForAnotherService_isRejected() {
    var token = TestTokens.forAudience("organizer").mint();

    assertThat(mvc.get().uri("/identity-probe").header(HttpHeaders.AUTHORIZATION, bearer(token)))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void request_withExpiredToken_isRejected() {
    var token = TestTokens.forAudience("mops").expired().mint();

    assertThat(mvc.get().uri("/identity-probe").header(HttpHeaders.AUTHORIZATION, bearer(token)))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void request_withValidToken_isDispatchedAsThatIdentity() {
    var principalId = UUID.randomUUID();
    var tenantId = UUID.randomUUID();
    var token =
        TestTokens.forAudience("mops")
            .principal(principalId)
            .tenant(tenantId)
            .scopes("mops:read")
            .mint();

    assertThat(mvc.get().uri("/identity-probe").header(HttpHeaders.AUTHORIZATION, bearer(token)))
        .hasStatusOk()
        .bodyJson()
        .isLenientlyEqualTo(
            """
            {"principalId":"%s","tenantId":"%s","scopes":["mops:read"]}
            """
                .formatted(principalId, tenantId));
  }

  @Test
  void request_withValidToken_reachesGraphql() {
    var token = TestTokens.forAudience("mops").mint();

    assertThat(
            mvc.post()
                .uri("/graphql")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(QUERY))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.allBudgets")
        .isNotNull();
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }
}
