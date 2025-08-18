package lab.mops.core.adapter.api.gql.activity;

import static org.assertj.core.api.Assertions.assertThat;

import lab.mops.core.domain.activity.Activity;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class ActivityMapperTest extends TestBase {

  ActivityMapper activityMapper = new ActivityMapper();

  @Test
  void map_mapsId() {
    var domainActivity = Activity.create(randomString());

    var result = activityMapper.map(domainActivity);

    assertThat(result.getId()).isEqualTo(domainActivity.getId().toString());
  }

  @Test
  void map_mapsName() {
    var name = randomString();
    var domainActivity = Activity.create(name);

    var result = activityMapper.map(domainActivity);

    assertThat(result.getName()).isEqualTo(domainActivity.getName());
  }

  @Test
  void map_mapsCreatedAt() {
    var domainActivity = Activity.create(randomString());

    var result = activityMapper.map(domainActivity);

    assertThat(result.getCreatedAt()).isEqualTo(domainActivity.getCreatedAt().toString());
  }

  @Test
  void map_mapsUpdatedAt() {
    var domainActivity = Activity.create(randomString());

    var result = activityMapper.map(domainActivity);

    assertThat(result.getUpdatedAt()).isEqualTo(domainActivity.getUpdatedAt().toString());
  }
}
