package nz.geek.jack.mops.iam.adapter.persistence;

import nz.geek.jack.libs.ddd.persistence.InMemoryAggregateStore;
import nz.geek.jack.mops.iam.domain.identity.Tenant;
import nz.geek.jack.mops.iam.domain.identity.TenantId;
import nz.geek.jack.mops.iam.domain.identity.TenantRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTenantRepository extends InMemoryAggregateStore<TenantId, Tenant>
    implements TenantRepository {}
