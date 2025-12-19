package lab.mops.core.api.gql.activity;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.List;
import lab.mops.api.gql.types.Activity;
import lab.mops.core.application.activity.ActivityQueryService;

@DgsComponent
public class ActivityDataFetcher {

  private final ActivityQueryService activityQueryService;

  private final ActivityMapper activityMapper;

  public ActivityDataFetcher(
      ActivityQueryService activityQueryService, ActivityMapper activityMapper) {
    this.activityQueryService = activityQueryService;
    this.activityMapper = activityMapper;
  }

  @DgsQuery
  public List<Activity> allActivities() {
    return activityQueryService.findAll().stream().map(activityMapper::map).toList();
  }
}
