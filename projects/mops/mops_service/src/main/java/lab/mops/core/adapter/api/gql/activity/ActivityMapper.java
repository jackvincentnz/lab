package lab.mops.core.adapter.api.gql.activity;

import lab.mops.api.gql.types.Activity;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

  public Activity map(lab.mops.core.domain.activity.Activity activity) {
    return Activity.newBuilder()
        .id(activity.getId().toString())
        .name(activity.getName())
        .createdAt(activity.getCreatedAt().toString())
        .updatedAt(activity.getUpdatedAt().toString())
        .build();
  }
}
