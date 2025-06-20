package nz.geek.jack.mops.iam.application.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import nz.geek.jack.mops.iam.domain.identity.Tenant;
import nz.geek.jack.mops.iam.domain.identity.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantCommandServiceTest {

  @Mock TenantRepository tenantRepository;

  @InjectMocks TenantCommandService tenantCommandService;

  @Test
  void provisionTenant_returnsSavedTenant() {
    var tenantCaptor = ArgumentCaptor.forClass(Tenant.class);

    var tenant = tenantCommandService.provisionTenant(randomString());

    verify(tenantRepository).save(tenantCaptor.capture());
    assertThat(tenantCaptor.getValue()).isEqualTo(tenant);
  }

  @Test
  void provisionTenant_savesTenant_withId() {
    var tenantCaptor = ArgumentCaptor.forClass(Tenant.class);

    var tenant = tenantCommandService.provisionTenant(randomString());

    verify(tenantRepository).save(tenantCaptor.capture());
    assertThat(tenantCaptor.getValue().getId()).isEqualTo(tenant.getId());
  }

  @Test
  void provisionTenant_savesTenant_withName() {
    var name = randomString();
    var tenantCaptor = ArgumentCaptor.forClass(Tenant.class);

    tenantCommandService.provisionTenant(name);

    verify(tenantRepository).save(tenantCaptor.capture());
    assertThat(tenantCaptor.getValue().getName()).isEqualTo(name);
  }

  private String randomString() {
    return UUID.randomUUID().toString();
  }
}
