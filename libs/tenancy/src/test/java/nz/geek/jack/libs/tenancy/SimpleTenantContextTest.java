package nz.geek.jack.libs.tenancy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SimpleTenantContextTest {

  @Test
  void getTenantId_returnsTenant() {
    var tenantId = newId();

    var context = SimpleTenantContext.of(tenantId);

    assertThat(context.getTenantId()).isEqualTo(tenantId);
  }

  @Test
  void equals_sameInstance() {
    var context = newContext();
    var copy = context;

    assertThat(copy).isEqualTo(context);
  }

  @Test
  void equals_otherInstance() {
    var tenantId = newId();
    var first = SimpleTenantContext.of(tenantId);
    var second = SimpleTenantContext.of(tenantId);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_doesNotEqualDifferentTenant() {
    var tenantId1 = newId();
    var tenantId2 = newId();
    var first = SimpleTenantContext.of(tenantId1);
    var second = SimpleTenantContext.of(tenantId2);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_doesNotEqualNullOther() {
    var context = newContext();

    assertThat(context).isNotEqualTo(null);
  }

  @Test
  void equals_doesNotEqualOtherClass() {
    var tenantId = newId();
    var context = SimpleTenantContext.of(tenantId);
    var anotherContext =
        new TenantContext() {
          @Override
          public String getTenantId() {
            return tenantId;
          }
        };

    assertThat(context).isNotEqualTo(anotherContext);
  }

  @Test
  void hashCode_isSameForMatchingInstances() {
    var tenantId = newId();
    var first = SimpleTenantContext.of(tenantId);
    var second = SimpleTenantContext.of(tenantId);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  private SimpleTenantContext newContext() {
    var tenantId = newId();
    return SimpleTenantContext.of(tenantId);
  }

  private String newId() {
    return UUID.randomUUID().toString();
  }
}
