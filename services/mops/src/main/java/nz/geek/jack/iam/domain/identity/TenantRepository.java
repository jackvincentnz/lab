package nz.geek.jack.iam.domain.identity;

public interface TenantRepository {
  void save(Tenant tenant);
}
