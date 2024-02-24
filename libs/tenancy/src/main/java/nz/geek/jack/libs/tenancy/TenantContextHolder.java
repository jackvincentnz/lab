package nz.geek.jack.libs.tenancy;

import java.util.Optional;

public class TenantContextHolder {

  private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

  private TenantContextHolder() {}

  public static void set(TenantContext tenantContext) {
    CONTEXT.set(tenantContext);
  }

  public static Optional<TenantContext> get() {
    return Optional.ofNullable(CONTEXT.get());
  }

  public static void clear() {
    CONTEXT.remove();
  }
}
