package lab.mops.core.api.gql.activity;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static lab.mops.common.api.gql.ResponseMessage.CREATED_MESSAGE;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lab.mops.api.gql.types.CreateActivityInput;
import lab.mops.api.gql.types.CreateActivityResponse;
import lab.mops.core.application.activity.ActivityCommandService;
import lab.mops.core.application.activity.CreateActivityCommand;

@DgsComponent
public class ActivityMutation {

  private final ActivityCommandService activityCommandService;

  private final ActivityMapper activityMapper;

  public ActivityMutation(
      ActivityCommandService activityCommandService, ActivityMapper activityMapper) {
    this.activityCommandService = activityCommandService;
    this.activityMapper = activityMapper;
  }

  @DgsMutation
  public CreateActivityResponse createActivity(@InputArgument("input") CreateActivityInput input) {
    var command = new CreateActivityCommand(input.getName());

    var activity = activityCommandService.create(command);

    return CreateActivityResponse.newBuilder()
        .code(SC_CREATED)
        .success(true)
        .message(CREATED_MESSAGE)
        .activity(activityMapper.map(activity))
        .build();
  }
}
