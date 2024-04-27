package nz.geek.jack.iam.domain.identity;

import nz.geek.jack.libs.domain.DomainEvent;

public class TenantProvisionedEvent extends DomainEvent {

  private final TenantId id;

  private final String name;

  public static TenantProvisionedEvent of(TenantId id, String name) {
    return new TenantProvisionedEvent(id, name);
  }

  private TenantProvisionedEvent(TenantId id, String name) {
    this.id = id;
    this.name = name;
  }

  public TenantId getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
