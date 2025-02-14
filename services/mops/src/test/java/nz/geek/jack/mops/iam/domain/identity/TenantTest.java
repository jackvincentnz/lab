package nz.geek.jack.mops.iam.domain.identity;

import static nz.geek.jack.libs.ddd.domain.test.ESAggregateTestUtils.getOnlyEventOfType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantTest {

  @Test
  void provision_setsId() {
    var tenant = Tenant.provision(randomString());

    assertThat(tenant.getId()).isNotNull();
  }

  @Test
  void provision_setsName() {
    var name = randomString();

    var tenant = Tenant.provision(name);

    assertThat(tenant.getName()).isEqualTo(name);
  }

  @Test
  void provision_appliesTenantProvisionedEvent_withId() {
    var tenant = Tenant.provision(randomString());

    var event = getOnlyEventOfType(tenant, TenantProvisionedEvent.class);
    assertThat(event.getAggregateId()).isEqualTo(tenant.getId());
  }

  @Test
  void provision_appliesTenantProvisionedEvent_withName() {
    var name = randomString();

    var tenant = Tenant.provision(name);

    var event = getOnlyEventOfType(tenant, TenantProvisionedEvent.class);
    assertThat(event.getName()).isEqualTo(name);
  }

  private String randomString() {
    return UUID.randomUUID().toString();
  }
}
