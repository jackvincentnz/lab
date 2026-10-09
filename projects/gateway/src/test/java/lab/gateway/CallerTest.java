package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class CallerTest extends TestBase {

  @Test
  void of_formLoginSession_describesTheConfiguredUserAuthenticatedByForm() {
    var user =
        new GatewayUsers.ConfiguredUser(
            randomString(),
            "{noop}" + randomString(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(randomString(), randomString()));
    var principal = new GatewayPrincipal(user);
    var authentication =
        UsernamePasswordAuthenticationToken.authenticated(
            principal, null, principal.getAuthorities());

    var caller = Caller.of(authentication);

    assertThat(caller)
        .contains(new Caller(user.principal(), user.tenant(), user.scopes(), Caller.FORM));
  }

  @Test
  void of_authenticationTheGatewayDidNotEstablish_isEmpty() {
    var authentication = new TestingAuthenticationToken(randomString(), randomString());

    assertThat(Caller.of(authentication)).isEmpty();
  }
}
