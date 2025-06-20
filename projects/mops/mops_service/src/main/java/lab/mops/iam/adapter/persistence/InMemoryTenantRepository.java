package lab.mops.iam.adapter.persistence;

import lab.mops.iam.domain.identity.Tenant;
import lab.mops.iam.domain.identity.TenantId;
import lab.mops.iam.domain.identity.TenantRepository;
import nz.geek.jack.libs.ddd.persistence.InMemoryAggregateStore;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTenantRepository extends InMemoryAggregateStore<TenantId, Tenant>
    implements TenantRepository {}
