package lab.libs.identity.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class IdentityClaimsTest {

  @Test
  void identity_isBuiltFromTheContractClaims() {
    var principalId = UUID.randomUUID();
    var tenantId = UUID.randomUUID();
    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject(principalId.toString())
            .expiresAt(Instant.now().plusSeconds(60))
            .claim(IdentityClaims.TENANT, tenantId.toString())
            .claim(IdentityClaims.SCOPE, "mops:read mops:write")
            .build();

    var identity = IdentityClaims.identity(jwt);

    assertThat(identity.principalId()).isEqualTo(principalId);
    assertThat(identity.tenantId()).isEqualTo(tenantId);
    assertThat(identity.scopes()).containsExactlyInAnyOrder("mops:read", "mops:write");
  }

  @Test
  void scopes_splitOnWhitespaceIgnoringEmptyEntries() {
    assertThat(IdentityClaims.scopes("  mops:read \t mops:write\n"))
        .containsExactlyInAnyOrder("mops:read", "mops:write");
  }

  @Test
  void scopes_areEmpty_forABlankString() {
    assertThat(IdentityClaims.scopes("   ")).isEmpty();
  }
}
