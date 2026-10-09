package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lab.libs.identity.jwt.IdentityClaims;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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
  void of_bearerJwt_describesTheTokenClaimsAuthenticatedByBearer() {
    var principal = UUID.randomUUID();
    var tenant = UUID.randomUUID();
    var scopes = List.of(randomString(), randomString());
    var jwt =
        Jwt.withTokenValue(randomString())
            .header("alg", "RS256")
            .subject(principal.toString())
            .issuedAt(Instant.now())
            .claim(IdentityClaims.TENANT, tenant.toString())
            .claim(IdentityClaims.SCOPE, " " + String.join("  ", scopes) + " ")
            .build();

    var caller = Caller.of(new JwtAuthenticationToken(jwt));

    assertThat(caller).contains(new Caller(principal, tenant, scopes, Caller.BEARER));
  }

  @Test
  void of_authenticationTheGatewayDidNotEstablish_isEmpty() {
    var authentication = new TestingAuthenticationToken(randomString(), randomString());

    assertThat(Caller.of(authentication)).isEmpty();
  }
}
