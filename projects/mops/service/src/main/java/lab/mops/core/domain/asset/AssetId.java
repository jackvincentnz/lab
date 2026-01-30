package lab.mops.core.domain.asset;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;

public final class AssetId extends InternalId {

  private AssetId() {
    super();
  }

  private AssetId(UUID id) {
    super(id);
  }

  public static AssetId create() {
    return new AssetId();
  }

  public static AssetId fromString(String id) {
    return new AssetId(UUID.fromString(id));
  }
}
