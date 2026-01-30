package lab.mops.iam.domain.identity;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;

public class TenantId extends InternalId {

  private TenantId() {
    super();
  }

  private TenantId(UUID id) {
    super(id);
  }

  public static TenantId create() {
    return new TenantId();
  }

  public static TenantId fromString(String id) {
    return new TenantId(UUID.fromString(id));
  }
}
