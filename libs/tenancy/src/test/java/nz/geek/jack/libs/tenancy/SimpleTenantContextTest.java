package nz.geek.jack.libs.tenancy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SimpleTenantContextTest {

  @Test
  void getTenant_returnsTenant() {
    var tenant = "tenant";

    var context = SimpleTenantContext.of(tenant);

    assertThat(context.getTenant()).isEqualTo(tenant);
  }

  @Test
  void equals_sameInstance() {
    var context = newContext();
    var copy = context;

    assertThat(copy).isEqualTo(context);
  }

  @Test
  void equals_otherInstance() {
    var tenant = newId();
    var first = SimpleTenantContext.of(tenant);
    var second = SimpleTenantContext.of(tenant);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_doesNotEqualDifferentId() {
    var tenant1 = newId();
    var tenant2 = newId();
    var first = SimpleTenantContext.of(tenant1);
    var second = SimpleTenantContext.of(tenant2);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_doesNotEqualNullOther() {
    var context = newContext();

    assertThat(context).isNotEqualTo(null);
  }

  @Test
  void equals_doesNotEqualOtherClass() {
    var tenant = newId();
    var context = SimpleTenantContext.of(tenant);
    var anotherContext =
        new TenantContext() {
          @Override
          public String getTenant() {
            return tenant;
          }
        };

    assertThat(context).isNotEqualTo(anotherContext);
  }

  @Test
  void hashCode_isSameForMatchingInstances() {
    var tenant = newId();
    var first = SimpleTenantContext.of(tenant);
    var second = SimpleTenantContext.of(tenant);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  private SimpleTenantContext newContext() {
    var tenant = newId();
    return SimpleTenantContext.of(tenant);
  }

  private String newId() {
    return UUID.randomUUID().toString();
  }
}
