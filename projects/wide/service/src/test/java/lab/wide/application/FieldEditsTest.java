package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
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
  void editFields_setField_returnsTheChange() {
    var title = randomString();

    var changes = FieldEdits.editFields(Map.of("title", title), null, ALLOWED, REQUIRED);

    assertThat(changes).containsExactly(Map.entry("title", title));
  }

  @Test
  void editFields_clearedField_returnsItAsNull() {
    var changes = FieldEdits.editFields(null, List.of("impact"), ALLOWED, REQUIRED);

    assertThat(changes).containsOnlyKeys("impact").containsEntry("impact", null);
  }

  @Test
  void editFields_setAndClearedDifferentFields_returnsBoth() {
    var title = randomString();

    var changes =
        FieldEdits.editFields(Map.of("title", title), List.of("impact"), ALLOWED, REQUIRED);

    assertThat(changes).containsEntry("title", title).containsEntry("impact", null);
  }

  @Test
  void editFields_nullValue_throws() {
    var fields = new HashMap<String, String>();
    fields.put("impact", null);

    assertThatThrownBy(() -> FieldEdits.editFields(fields, null, ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Use clearFields to clear an optional field.");
  }

  @Test
  void editFields_unknownClearField_throws() {
    assertThatThrownBy(() -> FieldEdits.editFields(null, List.of("typo"), ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown field to clear: typo");
  }

  @Test
  void editFields_nullClearField_throws() {
    var clearFields = Arrays.<String>asList((String) null);

    assertThatThrownBy(() -> FieldEdits.editFields(null, clearFields, ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown field to clear: null");
  }

  @Test
  void editFields_requiredFieldCleared_throws() {
    assertThatThrownBy(() -> FieldEdits.editFields(null, List.of("title"), ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot clear required field: title");
  }

  @Test
  void editFields_sameFieldSetAndCleared_throws() {
    var fields = Map.of("impact", randomString());

    assertThatThrownBy(() -> FieldEdits.editFields(fields, List.of("impact"), ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot set and clear the same field: impact");
  }

  @Test
  void editFields_emptyEdit_throws() {
    assertThatThrownBy(() -> FieldEdits.editFields(Map.of(), List.of(), ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide at least one field to edit or clear.");
  }

  @Test
  void editFields_noEdit_throws() {
    assertThatThrownBy(() -> FieldEdits.editFields(null, null, ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide at least one field to edit or clear.");
  }

  @Test
  void editFields_unknownSetField_throws() {
    var fields = Map.of("typo", randomString());

    assertThatThrownBy(() -> FieldEdits.editFields(fields, null, ALLOWED, REQUIRED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("Unknown editable field: typo");
  }

  @Test
  void edited_keyChanged_returnsTheChange() {
    var change = randomString();

    var value = FieldEdits.edited(Map.of("title", change), "title", randomString());

    assertThat(value).isEqualTo(change);
  }

  @Test
  void edited_keyAbsent_returnsTheCurrentValue() {
    var current = randomString();

    var value = FieldEdits.edited(Map.of(), "title", current);

    assertThat(value).isEqualTo(current);
  }
}
