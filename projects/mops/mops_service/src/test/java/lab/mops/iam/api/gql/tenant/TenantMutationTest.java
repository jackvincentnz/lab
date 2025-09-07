package lab.mops.iam.api.gql.tenant;

import static jakarta.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import lab.mops.api.gql.types.ProvisionTenantInput;
import lab.mops.api.gql.types.Tenant;
import lab.mops.iam.application.identity.TenantCommandService;
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
    var tenant = lab.mops.iam.domain.identity.Tenant.provision(name);
    var gqlTenant = Tenant.newBuilder().build();

    when(tenantCommandService.provisionTenant(name)).thenReturn(tenant);
    when(tenantMapper.map(tenant)).thenReturn(gqlTenant);

    var result = tenantMutation.provisionTenant(input);

    assertThat(result.getTenant()).isEqualTo(gqlTenant);
  }

  @Test
  void provisionTenant_returnsSuccess() {
    var name = randomString();
    var input = ProvisionTenantInput.newBuilder().name(name).build();
    var tenant = lab.mops.iam.domain.identity.Tenant.provision(name);
    var gqlTenant = Tenant.newBuilder().build();

    when(tenantCommandService.provisionTenant(name)).thenReturn(tenant);
    when(tenantMapper.map(tenant)).thenReturn(gqlTenant);

    var result = tenantMutation.provisionTenant(input);

    assertThat(result.getCode()).isEqualTo(SC_ACCEPTED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(TenantMutation.PROVISION_TENANT_SUCCESS_MESSAGE);
  }

  private String randomString() {
    return UUID.randomUUID().toString();
  }
}
