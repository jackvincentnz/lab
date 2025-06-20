package lab.mops.iam.adapter.api.gql.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lab.mops.iam.domain.identity.Tenant;
import org.junit.jupiter.api.Test;

class TenantMapperTest {

  TenantMapper mapper = new TenantMapper();

  @Test
  void map_mapsId() {
    var tenant = Tenant.provision(randomString());

    var result = mapper.map(tenant);

    assertThat(result.getId()).isEqualTo(tenant.getId().toString());
  }

  @Test
  void map_mapsName() {
    var tenant = Tenant.provision(randomString());

    var result = mapper.map(tenant);

    assertThat(result.getName()).isEqualTo(tenant.getName());
  }

  private String randomString() {
    return UUID.randomUUID().toString();
  }
}
