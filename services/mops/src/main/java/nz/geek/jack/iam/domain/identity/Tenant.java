package nz.geek.jack.iam.domain.identity;

import nz.geek.jack.libs.ddd.domain.Aggregate;

public class Tenant extends Aggregate {

  private TenantId id;

  private String name;

  public static Tenant provision(String name) {
    return new Tenant(name);
  }

  private Tenant(String name) {
    var tenantId = TenantId.create();
    apply(TenantProvisionedEvent.of(tenantId, name));
  }

  private void on(TenantProvisionedEvent event) {
    this.id = event.getId();
    this.name = event.getName();
  }

  public TenantId getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
