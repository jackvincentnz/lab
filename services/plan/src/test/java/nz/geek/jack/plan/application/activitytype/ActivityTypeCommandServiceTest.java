package nz.geek.jack.plan.application.activitytype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import nz.geek.jack.plan.domain.activitytype.ActivityType;
import nz.geek.jack.plan.domain.activitytype.ActivityTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityTypeCommandServiceTest {

  static final String NAME = "Tactic";

  @Mock ActivityTypeRepository activityTypeRepository;

  @InjectMocks private ActivityTypeCommandService activityTypeCommandService;

  @Test
  void createActivityType_savesActivityTypeWithName() {
    var activityTypeCaptor = ArgumentCaptor.forClass(ActivityType.class);

    activityTypeCommandService.createActivityType(NAME);

    verify(activityTypeRepository).saveActivityType(activityTypeCaptor.capture());
    assertThat(activityTypeCaptor.getValue().getName()).isEqualTo(NAME);
  }

  @Test
  void createActivityType_returnsSavedActivityTypeId() {
    var activityTypeCaptor = ArgumentCaptor.forClass(ActivityType.class);

    var activityTypeId = activityTypeCommandService.createActivityType(NAME);

    verify(activityTypeRepository).saveActivityType(activityTypeCaptor.capture());
    assertThat(activityTypeId).isEqualTo(activityTypeCaptor.getValue().getId());
  }
}
