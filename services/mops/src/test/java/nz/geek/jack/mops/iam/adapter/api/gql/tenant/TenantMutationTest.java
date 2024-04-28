package nz.geek.jack.mops.iam.adapter.api.gql.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import nz.geek.jack.mops.api.gql.types.ProvisionTenantInput;
import nz.geek.jack.mops.api.gql.types.Tenant;
import nz.geek.jack.mops.iam.application.identity.TenantCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantMutationTest {

  @Mock TenantCommandService tenantCommandService;

  @Mock TenantMapper tenantMapper;

  @InjectMocks TenantMutation tenantMutation;

  @Test
  void provisionTenant_provisionsTenant() {
    var input = ProvisionTenantInput.newBuilder().name(randomString()).build();

    tenantMutation.provisionTenant(input);

    verify(tenantCommandService).provisionTenant(input.getName());
  }

  @Test
  void provisionTenant_mapsAndReturnsTenant() {
    var name = randomString();
    var input = ProvisionTenantInput.newBuilder().name(name).build();
    var tenant = nz.geek.jack.mops.iam.domain.identity.Tenant.provision(name);
    var gqlTenant = Tenant.newBuilder().build();

    when(tenantCommandService.provisionTenant(name)).thenReturn(tenant);
    when(tenantMapper.map(tenant)).thenReturn(gqlTenant);

    var result = tenantMutation.provisionTenant(input);

    assertThat(result).isEqualTo(gqlTenant);
  }

  private String randomString() {
    return UUID.randomUUID().toString();
  }
}
