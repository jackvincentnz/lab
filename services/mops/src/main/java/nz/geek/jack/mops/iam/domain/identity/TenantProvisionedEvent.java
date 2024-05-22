package nz.geek.jack.mops.iam.domain.identity;

import nz.geek.jack.libs.ddd.domain.DomainEvent;

public class TenantProvisionedEvent extends DomainEvent<TenantId> {

  private final String name;

  public static TenantProvisionedEvent of(TenantId id, String name) {
    return new TenantProvisionedEvent(id, name);
  }

  private TenantProvisionedEvent(TenantId id, String name) {
    super(id);
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
