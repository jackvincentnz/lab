package nz.geek.jack.iam.adapter.api.gql.tenant;

import nz.geek.jack.iam.api.gql.types.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {
  public Tenant map(nz.geek.jack.iam.domain.identity.Tenant tenant) {
    return new Tenant(tenant.getId().toString(), tenant.getName());
  }
}
