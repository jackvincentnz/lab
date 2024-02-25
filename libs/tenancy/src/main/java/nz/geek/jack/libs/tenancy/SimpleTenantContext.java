package nz.geek.jack.libs.tenancy;

import java.util.Objects;

public final class SimpleTenantContext implements TenantContext {

  private final String tenantId;

  private SimpleTenantContext(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public String getTenantId() {
    return tenantId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimpleTenantContext that = (SimpleTenantContext) o;
    return Objects.equals(tenantId, that.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantId);
  }

  public static SimpleTenantContext of(String tenantId) {
    return new SimpleTenantContext(tenantId);
  }
}
