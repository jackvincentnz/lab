package lab.mops.core.application.tenant;

import lab.mops.core.domain.activityhierarchy.ActivityHierarchy;
import lab.mops.core.domain.activityhierarchy.ActivityHierarchyRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantEventHandler {

  private final ActivityHierarchyRepository activityHierarchyRepository;

  public TenantEventHandler(ActivityHierarchyRepository activityHierarchyRepository) {
    this.activityHierarchyRepository = activityHierarchyRepository;
  }

  public void handleTenantProvisionedEvent() {
    var activityHierarchy = ActivityHierarchy.create();

    activityHierarchyRepository.save(activityHierarchy);
  }
}
