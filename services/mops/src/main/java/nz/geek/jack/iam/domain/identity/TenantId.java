package nz.geek.jack.iam.domain.identity;

import java.util.UUID;
import nz.geek.jack.libs.domain.AbstractId;

public class TenantId extends AbstractId {

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
