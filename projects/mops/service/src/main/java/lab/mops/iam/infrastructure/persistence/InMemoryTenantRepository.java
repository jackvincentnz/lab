package lab.mops.iam.infrastructure.persistence;

import lab.libs.ddd.persistence.InMemoryAggregateStore;
import lab.mops.iam.domain.identity.Tenant;
import lab.mops.iam.domain.identity.TenantId;
import lab.mops.iam.domain.identity.TenantRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTenantRepository extends InMemoryAggregateStore<TenantId, Tenant>
    implements TenantRepository {}
