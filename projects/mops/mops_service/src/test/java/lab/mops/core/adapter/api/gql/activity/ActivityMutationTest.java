package lab.mops.core.adapter.api.gql.activity;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static lab.mops.core.adapter.api.gql.ResponseMessage.CREATED_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.api.gql.types.CreateActivityInput;
import lab.mops.core.application.activity.ActivityCommandService;
import lab.mops.core.application.activity.CreateActivityCommand;
import lab.mops.core.domain.activity.Activity;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityMutationTest extends TestBase {

  @Mock ActivityCommandService activityCommandService;

  @Mock ActivityMapper activityMapper;

  @InjectMocks ActivityMutation activityMutation;

  @Test
  void createActivity_creates() {
    var name = randomString();

    activityMutation.createActivity(CreateActivityInput.newBuilder().name(name).build());

    verify(activityCommandService).create(new CreateActivityCommand(name));
  }

  @Test
  void createActivity_mapsResponse() {
    var name = randomString();
    var domainActivity = mock(Activity.class);
    var graphActivity = mock(lab.mops.api.gql.types.Activity.class);

    when(activityCommandService.create(new CreateActivityCommand(name))).thenReturn(domainActivity);
    when(activityMapper.map(domainActivity)).thenReturn(graphActivity);

    var result =
        activityMutation.createActivity(CreateActivityInput.newBuilder().name(name).build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(CREATED_MESSAGE);
    assertThat(result.getActivity()).isEqualTo(graphActivity);
  }
}
