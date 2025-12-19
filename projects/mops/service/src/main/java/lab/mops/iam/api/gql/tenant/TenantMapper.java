package lab.mops.iam.api.gql.tenant;

import lab.mops.api.gql.types.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {
  public Tenant map(lab.mops.iam.domain.identity.Tenant tenant) {
    return new Tenant(tenant.getId().toString(), tenant.getName());
  }
}
