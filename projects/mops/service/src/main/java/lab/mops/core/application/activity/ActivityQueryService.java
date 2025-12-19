package lab.mops.core.application.activity;

import java.util.Collection;
import lab.mops.core.domain.activity.Activity;
import lab.mops.core.domain.activity.ActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityQueryService {

  private final ActivityRepository activityRepository;

  public ActivityQueryService(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  public Collection<Activity> findAll() {
    return activityRepository.findAll();
  }
}
