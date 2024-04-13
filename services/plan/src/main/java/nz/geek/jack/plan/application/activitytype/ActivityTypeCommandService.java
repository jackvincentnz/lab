package nz.geek.jack.plan.application.activitytype;

import nz.geek.jack.plan.domain.activitytype.ActivityType;
import nz.geek.jack.plan.domain.activitytype.ActivityTypeId;
import nz.geek.jack.plan.domain.activitytype.ActivityTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityTypeCommandService {

  private final ActivityTypeRepository activityTypeRepository;

  public ActivityTypeCommandService(ActivityTypeRepository activityTypeRepository) {
    this.activityTypeRepository = activityTypeRepository;
  }

  public ActivityTypeId createActivityType(String name) {
    var activityType = ActivityType.createActivityType(name);

    activityTypeRepository.saveActivityType(activityType);

    return activityType.getId();
  }
}
