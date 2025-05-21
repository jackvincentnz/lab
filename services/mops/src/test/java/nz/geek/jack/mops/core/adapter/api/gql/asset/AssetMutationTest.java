package nz.geek.jack.mops.core.adapter.api.gql.asset;

import nz.geek.jack.mops.core.application.asset.AssetCommandService;
import nz.geek.jack.mops.core.application.asset.CreateAssetCommand;
import nz.geek.jack.mops.core.domain.asset.AssetId;
import nz.geek.jack.mops.core.domain.asset.Asset; // Domain Asset
import nz.geek.jack.mops.types.CreateAssetInput; 
// import nz.geek.jack.mops.types.Asset; // This is the GraphQL type, used in response.getAsset()
import nz.geek.jack.mops.types.CreateAssetResponse;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor; // Added import

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetMutationTest {

    @Mock
    private AssetCommandService assetCommandService;

    @InjectMocks
    private AssetMutation assetMutation;

    @Test
    void testCreateAsset_success() {
        // 1. Arrange
        String assetName = "My New Asset";
        CreateAssetInput input = CreateAssetInput.newBuilder().name(assetName).build();

        AssetId mockAssetId = AssetId.create(); 

        nz.geek.jack.mops.core.domain.asset.Asset mockDomainAsset = 
            org.mockito.Mockito.mock(nz.geek.jack.mops.core.domain.asset.Asset.class);
        when(mockDomainAsset.getId()).thenReturn(mockAssetId);
        when(mockDomainAsset.getName()).thenReturn(assetName);

        when(assetCommandService.create(any(CreateAssetCommand.class)))
            .thenReturn(mockDomainAsset);

        // 2. Act
        CreateAssetResponse response = assetMutation.createAsset(input);

        // 3. Assert
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Asset created successfully");
        
        assertThat(response.getAsset()).isNotNull();
        assertThat(response.getAsset().getId()).isEqualTo(mockAssetId.toString());
        assertThat(response.getAsset().getName()).isEqualTo(assetName);

        ArgumentCaptor<CreateAssetCommand> commandCaptor = 
            ArgumentCaptor.forClass(CreateAssetCommand.class);
        verify(assetCommandService).create(commandCaptor.capture());
        assertThat(commandCaptor.getValue().name()).isEqualTo(assetName);
    }
}
