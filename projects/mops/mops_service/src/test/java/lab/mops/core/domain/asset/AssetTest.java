package lab.mops.core.domain.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class AssetTest extends TestBase {

  @Test
  void create_preventsNullName() {
    assertThatThrownBy(() -> Asset.create(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_setsId() {
    var asset = Asset.create(randomString());

    assertThat(asset.getId()).isNotNull();
  }

  @Test
  void create_registersEventWithId() {
    var asset = Asset.create(randomString());

    var event = (AssetCreatedEvent) asset.domainEvents().iterator().next();
    assertThat(event.id()).isEqualTo(asset.getId());
  }

  @Test
  void create_setsName() {
    var name = randomString();

    var asset = Asset.create(name);

    assertThat(asset.getName()).isEqualTo(name);
  }

  @Test
  void create_registersEventWithName() {
    var name = randomString();

    var asset = Asset.create(name);

    var event = (AssetCreatedEvent) asset.domainEvents().iterator().next();
    assertThat(event.name()).isEqualTo(asset.getName());
  }
}
