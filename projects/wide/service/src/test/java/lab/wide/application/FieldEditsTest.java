package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class FieldEditsTest extends TestBase {

  private static final Set<String> ALLOWED = Set.of("title", "impact");
  private static final Set<String> REQUIRED = Set.of("title");

  @Test
  void editFields_returnsRequestedChanges() {
    var title = randomString();

    var changes = FieldEdits.editFields(Map.of("title", title), null, ALLOWED, REQUIRED);

    assertThat(changes).containsExactly(Map.entry("title", title));
  }

  @Test
  void editFields_marksClearedFieldsNull() {
    var changes = FieldEdits.editFields(null, List.of("impact"), ALLOWED, REQUIRED);

    assertThat(changes).containsKey("impact");
    assertThat(changes.get("impact")).isNull();
  }

  @Test
  void editFields_rejectsNullValue() {
    var fields = new HashMap<String, String>();
    fields.put("impact", null);

    assertThatThrownBy(() -> FieldEdits.editFields(fields, null, ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Use clearFields to clear an optional field.");
  }

  @Test
  void editFields_rejectsUnknownClearField() {
    assertThatThrownBy(() -> FieldEdits.editFields(null, List.of("typo"), ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown field to clear: typo");
  }

  @Test
  void editFields_rejectsClearingRequiredField() {
    assertThatThrownBy(() -> FieldEdits.editFields(null, List.of("title"), ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot clear required field: title");
  }

  @Test
  void editFields_rejectsSettingAndClearingSameField() {
    assertThatThrownBy(
            () ->
                FieldEdits.editFields(
                    Map.of("impact", randomString()), List.of("impact"), ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot set and clear the same field: impact");
  }

  @Test
  void editFields_rejectsEmptyEdit() {
    assertThatThrownBy(() -> FieldEdits.editFields(Map.of(), List.of(), ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide at least one field to edit or clear.");
  }

  @Test
  void editFields_rejectsUnknownEditableField() {
    assertThatThrownBy(
            () -> FieldEdits.editFields(Map.of("typo", randomString()), null, ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("Unknown editable field: typo");
  }

  @Test
  void edited_returnsChangeWhenPresent() {
    var change = randomString();

    assertThat(FieldEdits.edited(Map.of("title", change), "title", randomString()))
        .isEqualTo(change);
  }

  @Test
  void edited_keepsCurrentValueWhenAbsent() {
    var current = randomString();

    assertThat(FieldEdits.edited(Map.of(), "title", current)).isEqualTo(current);
  }
}
