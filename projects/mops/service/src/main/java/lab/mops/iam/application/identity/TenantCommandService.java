package lab.mops.iam.application.identity;

import lab.mops.iam.domain.identity.Tenant;
import lab.mops.iam.domain.identity.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantCommandService {

  private final TenantRepository tenantRepository;

  public TenantCommandService(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  public Tenant provisionTenant(String name) {
    var tenant = Tenant.provision(name);

    tenantRepository.save(tenant);

    return tenant;
  }
}
