package lab.mops.iam.api.gql.tenant;

import static jakarta.servlet.http.HttpServletResponse.SC_ACCEPTED;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lab.mops.api.gql.types.ProvisionTenantInput;
import lab.mops.api.gql.types.ProvisionTenantResponse;
import lab.mops.iam.application.identity.TenantCommandService;

@DgsComponent
public class TenantMutation {

  protected static final String PROVISION_TENANT_SUCCESS_MESSAGE =
      "Tenant was successfully provisioned";

  private final TenantCommandService tenantCommandService;

  private final TenantMapper tenantMapper;

  public TenantMutation(TenantCommandService tenantCommandService, TenantMapper tenantMapper) {
    this.tenantCommandService = tenantCommandService;
    this.tenantMapper = tenantMapper;
  }

  @DgsMutation
  public ProvisionTenantResponse provisionTenant(
      @InputArgument("input") ProvisionTenantInput input) {
    var domainTenant = tenantCommandService.provisionTenant(input.getName());
    var gqlTenant = tenantMapper.map(domainTenant);

    return ProvisionTenantResponse.newBuilder()
        .code(SC_ACCEPTED)
        .success(true)
        .message(PROVISION_TENANT_SUCCESS_MESSAGE)
        .tenant(gqlTenant)
        .build();
  }
}
