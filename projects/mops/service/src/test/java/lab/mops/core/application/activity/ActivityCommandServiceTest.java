package lab.mops.core.application.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.core.domain.activity.Activity;
import lab.mops.core.domain.activity.ActivityRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityCommandServiceTest extends TestBase {

  @Mock ActivityRepository activityRepository;

  @InjectMocks ActivityCommandService activityCommandService;

  @Captor ArgumentCaptor<Activity> activityCaptor;

  @Test
  void create_savesActivityWithName() {
    var command = new CreateActivityCommand(randomString());

    activityCommandService.create(command);

    verify(activityRepository).save(activityCaptor.capture());
    assertThat(activityCaptor.getValue().getName()).isEqualTo(command.name());
  }

  @Test
  void create_returnsActivity() {
    var command = new CreateActivityCommand(randomString());
    var savedActivity = mock(Activity.class);

    when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);

    var result = activityCommandService.create(command);

    assertThat(result).isEqualTo(savedActivity);
  }
}
