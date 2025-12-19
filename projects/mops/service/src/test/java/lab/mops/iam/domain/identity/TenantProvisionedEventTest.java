package lab.mops.iam.domain.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantProvisionedEventTest {

  @Test
  void of_setsId() {
    var id = TenantId.create();

    var event = TenantProvisionedEvent.of(id, randomString());

    assertThat(event.getAggregateId()).isEqualTo(id);
  }

  @Test
  void of_setsName() {
    var name = randomString();

    var event = TenantProvisionedEvent.of(TenantId.create(), name);

    assertThat(event.getName()).isEqualTo(name);
  }

  private String randomString() {
    return UUID.randomUUID().toString();
  }
}
