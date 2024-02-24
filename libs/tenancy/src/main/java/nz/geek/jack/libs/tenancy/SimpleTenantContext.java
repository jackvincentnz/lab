package nz.geek.jack.libs.tenancy;

import java.util.Objects;

public final class SimpleTenantContext implements TenantContext {

  private final String tenant;

  private SimpleTenantContext(String tenant) {
    this.tenant = tenant;
  }

  @Override
  public String getTenant() {
    return tenant;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimpleTenantContext that = (SimpleTenantContext) o;
    return Objects.equals(tenant, that.tenant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenant);
  }

  public static SimpleTenantContext of(String tenant) {
    return new SimpleTenantContext(tenant);
  }
}
