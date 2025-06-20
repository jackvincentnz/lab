package lab.mops.core.domain.asset;

import nz.geek.jack.libs.ddd.domain.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends BaseRepository<Asset, AssetId> {}
