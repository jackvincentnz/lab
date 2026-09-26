package lab.libs.identity.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lab.libs.identity.web.IdentityHeaders.Absent;
import lab.libs.identity.web.IdentityHeaders.Invalid;
import lab.libs.identity.web.IdentityHeaders.Present;
import org.junit.jupiter.api.Test;

class IdentityHeadersTest {

  private final UUID principalId = UUID.randomUUID();
  private final UUID tenantId = UUID.randomUUID();

  @Test
  void parse_returnsAbsent_whenNoHeaders() {
    assertThat(IdentityHeaders.parse(null, null, null)).isInstanceOf(Absent.class);
  }

  @Test
  void parse_returnsPresent_whenAllHeadersValid() {
    var result =
        IdentityHeaders.parse(
            principalId.toString(), tenantId.toString(), "mops:read  mops:write ");

    assertThat(result).isInstanceOf(Present.class);
    var identity = ((Present) result).identity();
    assertThat(identity.principalId()).isEqualTo(principalId);
    assertThat(identity.tenantId()).isEqualTo(tenantId);
    assertThat(identity.scopes()).containsExactlyInAnyOrder("mops:read", "mops:write");
  }

  @Test
  void parse_returnsInvalid_whenPrincipalMissing() {
    var result = IdentityHeaders.parse(null, tenantId.toString(), "mops:read");

    assertThat(result).isEqualTo(new Invalid("missing X-Principal-Id"));
  }

  @Test
  void parse_returnsInvalid_whenTenantMalformed() {
    var result = IdentityHeaders.parse(principalId.toString(), "not-a-uuid", "mops:read");

    assertThat(result).isEqualTo(new Invalid("malformed X-Tenant-Id"));
  }

  @Test
  void parse_returnsInvalid_whenScopesMissing() {
    var result = IdentityHeaders.parse(principalId.toString(), tenantId.toString(), null);

    assertThat(result).isEqualTo(new Invalid("missing X-Scopes"));
  }

  @Test
  void parse_returnsInvalid_whenScopesBlank() {
    var result = IdentityHeaders.parse(principalId.toString(), tenantId.toString(), "   ");

    assertThat(result).isEqualTo(new Invalid("empty X-Scopes"));
  }
}
