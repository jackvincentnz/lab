package nz.geek.jack.iam.adapter.persistence;

import nz.geek.jack.iam.domain.identity.Tenant;
import nz.geek.jack.iam.domain.identity.TenantId;
import nz.geek.jack.iam.domain.identity.TenantRepository;
import nz.geek.jack.libs.ddd.persistence.InMemoryAggregateStore;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTenantRepository extends InMemoryAggregateStore<TenantId, Tenant>
    implements TenantRepository {}
