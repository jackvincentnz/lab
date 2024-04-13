package nz.geek.jack.plan.application.activity;

import nz.geek.jack.plan.domain.activity.Activity;
import nz.geek.jack.plan.domain.activity.ActivityId;
import nz.geek.jack.plan.domain.activity.ActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityCommandService {

  private final ActivityRepository activityRepository;

  public ActivityCommandService(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  public ActivityId createActivity(String name) {
    var activity = Activity.createActivity(name);

    activityRepository.saveActivity(activity);

    return activity.getId();
  }
}
