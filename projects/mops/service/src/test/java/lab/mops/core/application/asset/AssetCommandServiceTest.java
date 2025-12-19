package lab.mops.core.application.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.core.domain.asset.Asset;
import lab.mops.core.domain.asset.AssetRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssetCommandServiceTest extends TestBase {

  @Mock AssetRepository assetRepository;

  @InjectMocks AssetCommandService assetCommandService;

  @Captor ArgumentCaptor<Asset> assetCaptor;

  @Test
  void create_savesAssetWithName() {
    var command = new CreateAssetCommand(randomString());

    assetCommandService.create(command);

    verify(assetRepository).save(assetCaptor.capture());
    assertThat(assetCaptor.getValue().getName()).isEqualTo(command.name());
  }

  @Test
  void create_returnsAsset() {
    var command = new CreateAssetCommand(randomString());
    var savedAsset = mock(Asset.class);

    when(assetRepository.save(any(Asset.class))).thenReturn(savedAsset);

    var result = assetCommandService.create(command);

    assertThat(result).isEqualTo(savedAsset);
  }
}
