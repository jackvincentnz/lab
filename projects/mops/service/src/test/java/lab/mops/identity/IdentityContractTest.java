package lab.mops.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lab.libs.identity.web.IdentityHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Mops behind the gateway: every non-public request must carry the identity contract. */
@SpringBootTest
@AutoConfigureMockMvc
class IdentityContractTest {

  static final String QUERY = "{\"query\":\"{ allBudgets { id } }\"}";

  @Autowired MockMvcTester mvc;

  @Test
  void health_isPublic() {
    assertThat(mvc.get().uri("/actuator/health")).hasStatusOk();
  }

  @Test
  void request_withoutIdentity_isRejectedBeforeDispatch() {
    assertThat(mvc.post().uri("/graphql").contentType(MediaType.APPLICATION_JSON).content(QUERY))
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .hasBodyTextEqualTo("missing identity");
  }

  @Test
  void request_withMalformedIdentity_isRejectedBeforeDispatch() {
    assertThat(
            mvc.post()
                .uri("/graphql")
                .header(IdentityHeaders.PRINCIPAL_ID, "not-a-uuid")
                .header(IdentityHeaders.TENANT_ID, UUID.randomUUID().toString())
                .header(IdentityHeaders.SCOPES, "mops:read")
                .contentType(MediaType.APPLICATION_JSON)
                .content(QUERY))
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .hasBodyTextEqualTo("malformed X-Principal-Id");
  }

  @Test
  void request_withIdentity_isDispatched() {
    assertThat(
            mvc.post()
                .uri("/graphql")
                .header(IdentityHeaders.PRINCIPAL_ID, UUID.randomUUID().toString())
                .header(IdentityHeaders.TENANT_ID, UUID.randomUUID().toString())
                .header(IdentityHeaders.SCOPES, "mops:read mops:write")
                .contentType(MediaType.APPLICATION_JSON)
                .content(QUERY))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.allBudgets")
        .isNotNull();
  }
}
