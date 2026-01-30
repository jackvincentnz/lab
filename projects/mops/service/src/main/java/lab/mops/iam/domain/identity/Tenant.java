package lab.mops.iam.domain.identity;

import lab.libs.ddd.domain.EventSourcedAggregate;

public class Tenant extends EventSourcedAggregate<TenantId> {

  private String name;

  public static Tenant provision(String name) {
    return new Tenant(name);
  }

  private Tenant(String name) {
    var tenantId = TenantId.create();
    apply(TenantProvisionedEvent.of(tenantId, name));
  }

  private void on(TenantProvisionedEvent event) {
    this.id = event.getAggregateId();
    this.name = event.getName();
  }

  public String getName() {
    return name;
  }
}
