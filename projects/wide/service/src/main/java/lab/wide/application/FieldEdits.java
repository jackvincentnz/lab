package lab.wide.application;

import java.util.List;
import java.util.Map;
import java.util.Set;

final class FieldEdits {
  private FieldEdits() {}

  static Map<String, String> editFields(
      Map<String, String> fields,
      List<String> clearFields,
      Set<String> allowed,
      Set<String> required) {
    var changes = new java.util.HashMap<String, String>();
    if (fields != null) {
      for (var entry : fields.entrySet()) {
        if (entry.getValue() == null)
          throw new IllegalArgumentException("Use clearFields to clear an optional field.");
        changes.put(entry.getKey(), entry.getValue());
      }
    }
    if (clearFields != null) {
      for (String field : clearFields) {
        if (field == null || !allowed.contains(field))
          throw new IllegalArgumentException("Unknown field to clear: " + field);
        if (required.contains(field))
          throw new IllegalArgumentException("Cannot clear required field: " + field);
        if (fields != null && fields.containsKey(field))
          throw new IllegalArgumentException("Cannot set and clear the same field: " + field);
        changes.put(field, null);
      }
    }
    if (changes.isEmpty())
      throw new IllegalArgumentException("Provide at least one field to edit or clear.");
    for (String field : changes.keySet()) {
      if (!allowed.contains(field))
        throw new IllegalArgumentException(
            "Unknown editable field: " + field + ". Allowed fields: " + allowed);
    }
    return changes;
  }

  static String edited(Map<String, String> fields, String key, String current) {
    return fields.containsKey(key) ? fields.get(key) : current;
  }
}
