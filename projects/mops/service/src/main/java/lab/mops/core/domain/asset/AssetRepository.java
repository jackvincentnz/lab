package lab.mops.core.domain.asset;

import lab.libs.ddd.domain.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends BaseRepository<Asset, AssetId> {}
