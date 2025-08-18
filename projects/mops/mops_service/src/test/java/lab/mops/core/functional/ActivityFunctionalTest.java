package lab.mops.core.functional;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletResponse;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ActivityFunctionalTest extends TestBase {

  @Autowired TestClient client;

  @Test
  void createActivity_createsActivity() {
    var name = randomString();

    var response = client.createActivity(name);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_CREATED);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getActivity()).isNotNull();
  }

  @Test
  void allActivities_returnsAllActivities() {
    var name = randomString();

    client.createActivity(name);

    var activities = client.allActivities();

    assertThat(activities.size()).isEqualTo(1);
    assertThat(activities.get(0).getId()).isNotBlank();
    assertThat(activities.get(0).getName()).isEqualTo(name);
    assertThat(activities.get(0).getCreatedAt()).isNotBlank();
    assertThat(activities.get(0).getUpdatedAt()).isNotBlank();
  }
}
