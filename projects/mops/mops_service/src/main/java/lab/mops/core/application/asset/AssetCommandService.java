package lab.mops.core.application.asset;

import lab.mops.core.domain.asset.Asset;
import lab.mops.core.domain.asset.AssetRepository;
import org.springframework.stereotype.Component;

@Component
public class AssetCommandService {

  private final AssetRepository assetRepository;

  public AssetCommandService(AssetRepository assetRepository) {
    this.assetRepository = assetRepository;
  }

  public Asset create(CreateAssetCommand command) {
    var asset = Asset.create(command.name());

    return assetRepository.save(asset);
  }
}
