package lab.libs.identity.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lab.libs.identity.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

class IdentityJwtConverterTest {

  @Test
  void convert_buildsAuthenticatedIdentityWithScopeAuthorities() {
    var principalId = UUID.randomUUID();
    var tenantId = UUID.randomUUID();
    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim(JwtClaimNames.ISS, "issuer")
            .audience(List.of("mops"))
            .subject(principalId.toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .claim(IdentityClaims.TENANT, tenantId.toString())
            .claim(IdentityClaims.SCOPE, " mops:read   mops:write ")
            .build();

    var authentication = new IdentityJwtConverter().convert(jwt);

    assertThat(authentication.isAuthenticated()).isTrue();
    assertThat(authentication.identity())
        .isEqualTo(
            new Identity(principalId, tenantId, java.util.Set.of("mops:read", "mops:write")));
    assertThat(authentication.getPrincipal()).isSameAs(authentication.identity());
    assertThat(authentication.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("SCOPE_mops:read", "SCOPE_mops:write");
  }
}
