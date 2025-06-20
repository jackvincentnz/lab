package nz.geek.jack.mops.core.application.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import nz.geek.jack.mops.core.domain.activity.Activity;
import nz.geek.jack.mops.core.domain.activity.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityCommandServiceTest {

  static final String NAME = "Intro to Lab Webinar";

  @Mock ActivityRepository activityRepository;

  @InjectMocks private ActivityCommandService activityCommandService;

  @Test
  void createActivity_savesActivityWithName() {
    var activityCaptor = ArgumentCaptor.forClass(Activity.class);

    activityCommandService.createActivity(NAME);

    verify(activityRepository).saveActivity(activityCaptor.capture());
    assertThat(activityCaptor.getValue().getName()).isEqualTo(NAME);
  }

  @Test
  void createActivity_returnsSavedActivityId() {
    var activityCaptor = ArgumentCaptor.forClass(Activity.class);

    var activityId = activityCommandService.createActivity(NAME);

    verify(activityRepository).saveActivity(activityCaptor.capture());
    assertThat(activityId).isEqualTo(activityCaptor.getValue().getId());
  }
}
