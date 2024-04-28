package nz.geek.jack.mops.iam.adapter.api.gql.tenant;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import nz.geek.jack.mops.api.gql.types.ProvisionTenantInput;
import nz.geek.jack.mops.api.gql.types.Tenant;
import nz.geek.jack.mops.iam.application.identity.TenantCommandService;

@DgsComponent
public class TenantMutation {

  private final TenantCommandService tenantCommandService;

  private final TenantMapper tenantMapper;

  public TenantMutation(TenantCommandService tenantCommandService, TenantMapper tenantMapper) {
    this.tenantCommandService = tenantCommandService;
    this.tenantMapper = tenantMapper;
  }

  @DgsMutation
  public Tenant provisionTenant(@InputArgument("input") ProvisionTenantInput input) {
    var tenant = tenantCommandService.provisionTenant(input.getName());

    return tenantMapper.map(tenant);
  }
}
