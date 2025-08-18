package lab.mops.core.application.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import lab.mops.core.domain.activity.Activity;
import lab.mops.core.domain.activity.ActivityRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityCommandServiceTest extends TestBase {

  @Mock ActivityRepository activityRepository;

  @InjectMocks private ActivityCommandService activityCommandService;

  @Test
  void createActivity_savesActivityWithName() {
    var name = randomString();
    var activityCaptor = ArgumentCaptor.forClass(Activity.class);

    activityCommandService.createActivity(name);

    verify(activityRepository).save(activityCaptor.capture());
    assertThat(activityCaptor.getValue().getName()).isEqualTo(name);
  }

  @Test
  void createActivity_returnsSavedActivityId() {
    var activityCaptor = ArgumentCaptor.forClass(Activity.class);

    var activityId = activityCommandService.createActivity(randomString());

    verify(activityRepository).save(activityCaptor.capture());
    assertThat(activityId).isEqualTo(activityCaptor.getValue().getId());
  }
}
