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

/** Mops without a gateway: the direct dev target assumes a fixed development identity. */
@SpringBootTest(properties = "mops.identity.development.enabled=true")
@AutoConfigureMockMvc
class DevelopmentIdentityTest {

  static final String QUERY = "{\"query\":\"{ allBudgets { id } }\"}";

  @Autowired MockMvcTester mvc;

  @Test
  void request_withoutIdentity_isDispatchedAsDevelopmentIdentity() {
    assertThat(mvc.post().uri("/graphql").contentType(MediaType.APPLICATION_JSON).content(QUERY))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.allBudgets")
        .isNotNull();
  }

  @Test
  void request_withMalformedIdentity_isStillRejected() {
    assertThat(
            mvc.post()
                .uri("/graphql")
                .header(IdentityHeaders.PRINCIPAL_ID, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(QUERY))
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .hasBodyTextEqualTo("missing X-Tenant-Id");
  }
}
