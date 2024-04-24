package nz.geek.jack.plan.domain.activityhierarchy;

import static nz.geek.jack.libs.domain.test.AggregateTestUtils.getOnlyEventOfType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import nz.geek.jack.libs.domain.EventReductionException;
import org.junit.jupiter.api.Test;

class ActivityHierarchyTest {

  @Test
  void create_setsId() {
    var activityHierarchy = ActivityHierarchy.create();

    assertThat(activityHierarchy.getId()).isNotNull();
  }

  @Test
  void create_appliesActivityHierarchyCreatedEvent_withId() {
    var activityHierarchy = ActivityHierarchy.create();

    var event = getOnlyEventOfType(activityHierarchy, ActivityHierarchyCreatedEvent.class);
    assertThat(event.getActivityHierarchyId()).isEqualTo(activityHierarchy.getId());
  }

  @Test
  void addRootActivityType_addsRootType() {
    var activityTypeName = randomString();
    var activityHierarchy = ActivityHierarchy.create();

    assertThat(activityHierarchy.getRootActivityType()).isEmpty();
    assertThat(activityHierarchy.getActivityTypeCount()).isEqualTo(0);

    activityHierarchy.addRootActivityType(activityTypeName);

    assertThat(activityHierarchy.getRootActivityType()).isNotEmpty();
    assertThat(activityHierarchy.getActivityTypeCount()).isEqualTo(1);
  }

  @Test
  void addRootActivityType_appliesRootActivityTypeAddedEvent_withId() {
    var activityTypeName = randomString();
    var activityHierarchy = existingHierarchy();

    activityHierarchy.addRootActivityType(activityTypeName);

    var event = getOnlyEventOfType(activityHierarchy, RootActivityTypeAddedEvent.class);
    assertThat(event.getActivityTypeId()).isNotNull();
  }

  @Test
  void addRootActivityType_appliesRootActivityTypeAddedEvent_withName() {
    var activityTypeName = randomString();
    var activityHierarchy = existingHierarchy();

    activityHierarchy.addRootActivityType(activityTypeName);

    var event = getOnlyEventOfType(activityHierarchy, RootActivityTypeAddedEvent.class);
    assertThat(event.getName()).isEqualTo(activityTypeName);
  }

  @Test
  void addRootActivityType_mustBeAddedToEmptyHierarchy() {
    var activityTypeName = randomString();
    var activityHierarchy = ActivityHierarchy.create();
    activityHierarchy.addRootActivityType(activityTypeName);

    var thrown =
        assertThrows(
            EventReductionException.class,
            () -> activityHierarchy.addRootActivityType(activityTypeName));
    assertThat(thrown.getCause()).isInstanceOf(NotEmptyHierarchyException.class);
  }

  @Test
  void addRootActivityType_returnsAddedType() {
    var activityTypeName = randomString();
    var activityHierarchy = ActivityHierarchy.create();

    var activityType = activityHierarchy.addRootActivityType(activityTypeName);

    assertThat(activityType).isNotNull();
    assertThat(activityType.getName()).isEqualTo(activityTypeName);
  }

  private ActivityHierarchy existingHierarchy() {
    var activityHierarchy = ActivityHierarchy.create();
    activityHierarchy.flushEvents();
    return activityHierarchy;
  }

  private String randomString() {
    return UUID.randomUUID().toString();
  }
}
