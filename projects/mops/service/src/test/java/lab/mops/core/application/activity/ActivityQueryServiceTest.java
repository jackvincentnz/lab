package lab.mops.core.application.activity;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.mops.core.domain.activity.Activity;
import lab.mops.core.domain.activity.ActivityRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityQueryServiceTest extends TestBase {

  @Mock ActivityRepository activityRepository;

  @InjectMocks ActivityQueryService activityQueryService;

  @Test
  void findAll_delegatesToRepository() {
    var activities = List.of(Activity.create(randomString()));
    when(activityRepository.findAll()).thenReturn(activities);

    activityQueryService.findAll();

    verify(activityRepository).findAll();
  }
}
