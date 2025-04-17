package nz.geek.jack.mops.core.domain.asset;

import java.util.Objects;
import nz.geek.jack.libs.ddd.domain.Aggregate;

public class Asset extends Aggregate<AssetId> {

  private final String name;

  private Asset(AssetId id, String name) {
    super(id);
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static Asset create(String name) {
    var asset = new Asset(AssetId.create(), name);

    asset.registerEvent(new AssetCreatedEvent(asset.id, asset.getName()));

    return asset;
  }
}
