package lab.gateway;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.List;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;

class SecurityConfigurationTest extends TestBase {

  @Test
  void bearerClaimsValidator_withoutAudience_fails() {
    assertBearerClaimsValidatorFails(List.of());
  }

  @Test
  void bearerClaimsValidator_withTwoAudiences_fails() {
    assertBearerClaimsValidatorFails(List.of(randomString(), randomString()));
  }

  private void assertBearerClaimsValidatorFails(List<String> audiences) {
    var properties = new OAuth2ResourceServerProperties();
    properties.getJwt().setIssuerUri(randomString());
    properties.getJwt().setAudiences(audiences);

    assertThatIllegalStateException()
        .isThrownBy(() -> new SecurityConfiguration().bearerClaimsValidator(properties))
        .withMessageContaining("spring.security.oauth2.resourceserver.jwt.audiences");
  }
}
