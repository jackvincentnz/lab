package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;

class SecurityConfigurationTest extends TestBase {

  private final SecurityConfiguration securityConfiguration = new SecurityConfiguration();

  @Test
  void bearerClaimsValidator_withoutAudience_fails() {
    var properties = new OAuth2ResourceServerProperties();
    properties.getJwt().setIssuerUri(randomString());
    properties.getJwt().setAudiences(List.of());

    var thrown = catchThrowable(() -> securityConfiguration.bearerClaimsValidator(properties));

    assertThat(thrown)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("spring.security.oauth2.resourceserver.jwt.audiences");
  }

  @Test
  void bearerClaimsValidator_withTwoAudiences_fails() {
    var properties = new OAuth2ResourceServerProperties();
    properties.getJwt().setIssuerUri(randomString());
    properties.getJwt().setAudiences(List.of(randomString(), randomString()));

    var thrown = catchThrowable(() -> securityConfiguration.bearerClaimsValidator(properties));

    assertThat(thrown)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("spring.security.oauth2.resourceserver.jwt.audiences");
  }
}
