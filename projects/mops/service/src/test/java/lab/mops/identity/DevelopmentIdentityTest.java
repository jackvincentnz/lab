package lab.mops.identity;

import static org.assertj.core.api.Assertions.assertThat;

import lab.libs.identity.testing.TestTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Mops without an issuer: the direct local target assumes a fixed development identity. */
@SpringBootTest(
    properties = {
      "mops.identity.development.enabled=true",
      "mops.identity.jwk-set-uri=http://[::1]:1/.well-known/jwks.json"
    })
@AutoConfigureMockMvc
@Import(IdentityProbe.class)
class DevelopmentIdentityTest {

  static final String QUERY = "{\"query\":\"{ allBudgets { id } }\"}";

  @Autowired MockMvcTester mvc;

  @Test
  void request_withoutToken_isDispatchedAsDevelopmentIdentity() {
    assertThat(mvc.get().uri("/identity-probe"))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.principalId")
        .isEqualTo("00000000-0000-0000-0000-000000000001");
  }

  @Test
  void request_withoutToken_reachesGraphql() {
    assertThat(mvc.post().uri("/graphql").contentType(MediaType.APPLICATION_JSON).content(QUERY))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.allBudgets")
        .isNotNull();
  }

  @Test
  void request_withInvalidToken_isStillRejected() {
    assertThat(mvc.get().uri("/identity-probe").header(HttpHeaders.AUTHORIZATION, "Bearer nope"))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  /**
   * The vertical could not evaluate the token, so the caller is not told it was rejected: the
   * request fails as a server error, which the servlet container reports as 500.
   */
  @Test
  void request_withWellFormedToken_failsServerSide_whenTheIssuerKeysAreUnreachable() {
    var token = TestTokens.forAudience("mops").mint();

    assertThat(
            mvc.get().uri("/identity-probe").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .hasFailed()
        .failure()
        .isInstanceOf(AuthenticationServiceException.class);
  }
}
