package nz.geek.jack.mops.iam.adapter.api.gql.tenant;

import nz.geek.jack.mops.api.gql.types.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {
  public Tenant map(nz.geek.jack.mops.iam.domain.identity.Tenant tenant) {
    return new Tenant(tenant.getId().toString(), tenant.getName());
  }
}
