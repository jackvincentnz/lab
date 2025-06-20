package nz.geek.jack.mops.core.domain.activityhierarchy;

import static nz.geek.jack.libs.ddd.domain.test.ESAggregateTestUtils.getOnlyEventOfType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.EventReductionException;
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
    assertThat(event.getAggregateId()).isEqualTo(activityHierarchy.getId());
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
  void addRootActivityType_appliesRootActivityTypeAddedEvent_withActivityHierarchyId() {
    var activityTypeName = randomString();
    var activityHierarchy = existingHierarchy();

    activityHierarchy.addRootActivityType(activityTypeName);

    var event = getOnlyEventOfType(activityHierarchy, RootActivityTypeAddedEvent.class);
    assertThat(event.getAggregateId()).isEqualTo(activityHierarchy.getId());
  }

  @Test
  void addRootActivityType_appliesRootActivityTypeAddedEvent_withActivityTypeId() {
    var activityTypeName = randomString();
    var activityHierarchy = existingHierarchy();

    var activityType = activityHierarchy.addRootActivityType(activityTypeName);

    var event = getOnlyEventOfType(activityHierarchy, RootActivityTypeAddedEvent.class);
    assertThat(event.getActivityTypeId()).isEqualTo(activityType.getId());
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

  @Test
  void addNestedActivityType_addsNestedType() {
    var activityHierarchy = existingHierarchyWithRoot();
    var rootActivityType = activityHierarchy.getRootActivityType().orElseThrow();

    assertThat(activityHierarchy.getActivityTypeCount()).isEqualTo(1);

    activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());

    assertThat(activityHierarchy.getActivityTypeCount()).isEqualTo(2);
  }

  @Test
  void addNestedActivityType_addsNestedTypeAsChildOfParent() {
    var activityHierarchy = existingHierarchyWithRoot();
    var rootActivityType = activityHierarchy.getRootActivityType().orElseThrow();

    var activityType =
        activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());

    assertThat(rootActivityType.hasChild(activityType)).isTrue();
  }

  @Test
  void addNestedActivityType_appliesNestedActivityTypeAddedEvent_withActivityHierarchyId() {
    var activityHierarchy = existingHierarchyWithRoot();
    var rootActivityType = activityHierarchy.getRootActivityType().orElseThrow();

    activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());

    var event = getOnlyEventOfType(activityHierarchy, NestedActivityTypeAddedEvent.class);
    assertThat(event.getAggregateId()).isEqualTo(activityHierarchy.getId());
  }

  @Test
  void addNestedActivityType_appliesNestedActivityTypeAddedEvent_withActivityTypeId() {
    var activityHierarchy = existingHierarchyWithRoot();
    var rootActivityType = activityHierarchy.getRootActivityType().orElseThrow();

    var activityType =
        activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());

    var event = getOnlyEventOfType(activityHierarchy, NestedActivityTypeAddedEvent.class);
    assertThat(event.getActivityTypeId()).isEqualTo(activityType.getId());
  }

  @Test
  void addNestedActivityType_appliesNestedActivityTypeAddedEvent_withName() {
    var activityTypeName = randomString();
    var activityHierarchy = existingHierarchyWithRoot();
    var rootActivityType = activityHierarchy.getRootActivityType().orElseThrow();

    activityHierarchy.addNestedActivityType(activityTypeName, rootActivityType.getId());

    var event = getOnlyEventOfType(activityHierarchy, NestedActivityTypeAddedEvent.class);
    assertThat(event.getName()).isEqualTo(activityTypeName);
  }

  @Test
  void addNestedActivityType_appliesNestedActivityTypeAddedEvent_withParentId() {
    var activityHierarchy = existingHierarchyWithRoot();
    var rootActivityType = activityHierarchy.getRootActivityType().orElseThrow();

    activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());

    var event = getOnlyEventOfType(activityHierarchy, NestedActivityTypeAddedEvent.class);
    assertThat(event.getParentId()).isEqualTo(rootActivityType.getId());
  }

  @Test
  void addNestedActivityType_mustHaveUniqueName() {
    var activityHierarchy = existingHierarchyWithRoot();
    var rootActivityType = activityHierarchy.getRootActivityType().orElseThrow();

    var thrown =
        assertThrows(
            EventReductionException.class,
            () ->
                activityHierarchy.addNestedActivityType(
                    rootActivityType.getName(), rootActivityType.getId()));
    assertThat(thrown.getCause()).isInstanceOf(DuplicateActivityTypeNameException.class);
  }

  @Test
  void addNestedActivityType_parentMustExist() {
    var activityHierarchy = existingHierarchy();

    var thrown =
        assertThrows(
            EventReductionException.class,
            () -> activityHierarchy.addNestedActivityType(randomString(), ActivityTypeId.create()));
    assertThat(thrown.getCause()).isInstanceOf(ActivityTypeNotFoundException.class);
  }

  private ActivityHierarchy existingHierarchy() {
    var activityHierarchy = ActivityHierarchy.create();
    activityHierarchy.flushEvents();
    return activityHierarchy;
  }

  private ActivityHierarchy existingHierarchyWithRoot() {
    var activityHierarchy = existingHierarchy();

    var rootActivityTypeName = randomString();
    activityHierarchy.addRootActivityType(rootActivityTypeName);

    activityHierarchy.flushEvents();

    return activityHierarchy;
  }

  private String randomString() {
    return UUID.randomUUID().toString();
  }
}
