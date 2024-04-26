package nz.geek.jack.mops.plan.domain.activityhierarchy;

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
  void addNestedActivityType_appliesNestedActivityTypeAddedEvent_withId() {
    var activityHierarchy = existingHierarchyWithRoot();
    var rootActivityType = activityHierarchy.getRootActivityType().orElseThrow();

    var activityType =
        activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());

    var event = getOnlyEventOfType(activityHierarchy, NestedActivityTypeAddedEvent.class);
    assertThat(event.getId()).isEqualTo(activityType.getId());
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

  @Test
  void nestActivityType_validatesParentExists() {
    var activityHierarchy = existingHierarchy();
    var existingType = activityHierarchy.addRootActivityType(randomString());

    var thrown =
        assertThrows(
            EventReductionException.class,
            () ->
                activityHierarchy.nestActivityType(ActivityTypeId.create(), existingType.getId()));
    assertThat(thrown.getCause()).isInstanceOf(ActivityTypeNotFoundException.class);
  }

  @Test
  void nestActivityType_validatesChildExists() {
    var activityHierarchy = existingHierarchy();
    var existingType = activityHierarchy.addRootActivityType(randomString());

    var thrown =
        assertThrows(
            EventReductionException.class,
            () ->
                activityHierarchy.nestActivityType(existingType.getId(), ActivityTypeId.create()));
    assertThat(thrown.getCause()).isInstanceOf(ActivityTypeNotFoundException.class);
  }

  @Test
  void nestActivityType_addsChildToParent() {
    var activityHierarchy = existingHierarchy();
    var rootActivityType = activityHierarchy.addRootActivityType(randomString());

    // Add two types under root
    var parent = activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());
    var child = activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());

    // Nest child under parent
    activityHierarchy.nestActivityType(parent.getId(), child.getId());

    assertThat(parent.hasChild(child)).isTrue();
  }

  @Test
  void nestActivityType_appliesActivityTypeNestedEvent_withParentId() {
    var activityHierarchy = existingHierarchy();
    var rootActivityType = activityHierarchy.addRootActivityType(randomString());

    // Add two types under root
    var parent = activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());
    var child = activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());
    activityHierarchy.flushEvents();

    // Nest child under parent
    activityHierarchy.nestActivityType(parent.getId(), child.getId());

    var event = getOnlyEventOfType(activityHierarchy, ActivityTypeNestedEvent.class);
    assertThat(event.getParentId()).isEqualTo(parent.getId());
  }

  @Test
  void nestActivityType_appliesActivityTypeNestedEvent_withChildId() {
    var activityHierarchy = existingHierarchy();
    var rootActivityType = activityHierarchy.addRootActivityType(randomString());

    // Add two types under root
    var parent = activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());
    var child = activityHierarchy.addNestedActivityType(randomString(), rootActivityType.getId());
    activityHierarchy.flushEvents();

    // Nest child under parent
    activityHierarchy.nestActivityType(parent.getId(), child.getId());

    var event = getOnlyEventOfType(activityHierarchy, ActivityTypeNestedEvent.class);
    assertThat(event.getChildId()).isEqualTo(child.getId());
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
