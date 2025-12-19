package lab.mops.core.application.activity;

import lab.mops.core.domain.activity.Activity;
import lab.mops.core.domain.activity.ActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityCommandService {

  private final ActivityRepository activityRepository;

  public ActivityCommandService(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  public Activity create(CreateActivityCommand command) {
    var activity = Activity.create(command.name());

    return activityRepository.save(activity);
  }
}
