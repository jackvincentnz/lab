package nz.geek.jack.mops.core.application.tenant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import nz.geek.jack.mops.core.domain.activityhierarchy.ActivityHierarchy;
import nz.geek.jack.mops.core.domain.activityhierarchy.ActivityHierarchyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantEventHandlerTest {

  @Mock ActivityHierarchyRepository activityHierarchyRepository;

  @InjectMocks TenantEventHandler tenantEventHandler;

  @Test
  void handleTenantProvisionedEvent_savesNewHierarchy() {
    tenantEventHandler.handleTenantProvisionedEvent();

    verify(activityHierarchyRepository).save(any(ActivityHierarchy.class));
  }
}
