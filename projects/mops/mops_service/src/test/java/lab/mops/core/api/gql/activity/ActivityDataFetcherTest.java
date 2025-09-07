package lab.mops.core.api.gql.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.mops.api.gql.types.Activity;
import lab.mops.core.application.activity.ActivityQueryService;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityDataFetcherTest extends TestBase {

  @Mock ActivityQueryService activityQueryService;

  @Mock ActivityMapper activityMapper;

  @InjectMocks ActivityDataFetcher activityDataFetcher;

  @Test
  void allCategories_mapsCategories() {
    var domainActivity = newActivity();
    var graphActivity = Activity.newBuilder().build();
    when(activityQueryService.findAll()).thenReturn(List.of(domainActivity));
    when(activityMapper.map(domainActivity)).thenReturn(graphActivity);

    var result = activityDataFetcher.allActivities();

    assertThat(result).contains(graphActivity);
  }

  private lab.mops.core.domain.activity.Activity newActivity() {
    return lab.mops.core.domain.activity.Activity.create(randomString());
  }
}
