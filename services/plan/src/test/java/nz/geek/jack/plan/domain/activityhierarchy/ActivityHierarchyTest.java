package nz.geek.jack.plan.domain.activityhierarchy;

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
  void create_appliesActivityHierarchyCreatedEvent() {
    var activityHierarchy = ActivityHierarchy.create();

    var events = activityHierarchy.flushEvents();
    var event = events.get(0);

    assertThat(events.size()).isEqualTo(1);
    assertThat(event).isInstanceOf(ActivityHierarchyCreatedEvent.class);
  }

  @Test
  void create_appliesActivityHierarchyCreatedEvent_withId() {
    var activityHierarchy = ActivityHierarchy.create();

    var events = activityHierarchy.flushEvents();
    var event = (ActivityHierarchyCreatedEvent) events.get(0);

    assertThat(event.getActivityHierarchyId()).isEqualTo(activityHierarchy.getId());
  }

  @Test
  void addActivityType_addsType() {
    var activityTypeName = randomString();
    var activityHierarchy = ActivityHierarchy.create();

    assertThat(activityHierarchy.getActivityTypeCount()).isEqualTo(0);

    activityHierarchy.addActivityType(activityTypeName);

    assertThat(activityHierarchy.getActivityTypeCount()).isEqualTo(1);
  }

  @Test
  void addActivityType_appliesActivityTypeAddedEvent() {
    var activityTypeName = randomString();
    var activityHierarchy = existingHierarchy();

    activityHierarchy.addActivityType(activityTypeName);

    var events = activityHierarchy.flushEvents();
    var event = events.get(0);

    assertThat(events.size()).isEqualTo(1);
    assertThat(event).isInstanceOf(ActivityTypeAddedEvent.class);
  }

  @Test
  void addActivityType_appliesActivityTypeAddedEvent_withId() {
    var activityTypeName = randomString();
    var activityHierarchy = existingHierarchy();

    activityHierarchy.addActivityType(activityTypeName);

    var events = activityHierarchy.flushEvents();
    var event = (ActivityTypeAddedEvent) events.get(0);

    assertThat(event.getActivityTypeId()).isNotNull();
  }

  @Test
  void addActivityType_appliesActivityTypeAddedEvent_withName() {
    var activityTypeName = randomString();
    var activityHierarchy = existingHierarchy();

    activityHierarchy.addActivityType(activityTypeName);

    var events = activityHierarchy.flushEvents();
    var event = (ActivityTypeAddedEvent) events.get(0);

    assertThat(event.getName()).isEqualTo(activityTypeName);
  }

  @Test
  void addActivityType_mustUseUniqueName() {
    var activityTypeName = randomString();
    var activityHierarchy = ActivityHierarchy.create();
    activityHierarchy.addActivityType(activityTypeName);

    var thrown =
        assertThrows(
            EventReductionException.class,
            () -> activityHierarchy.addActivityType(activityTypeName));
    assertThat(thrown.getCause()).isInstanceOf(DuplicateActivityTypeNameException.class);
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
