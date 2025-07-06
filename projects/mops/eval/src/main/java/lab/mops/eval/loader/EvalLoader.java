package lab.mops.eval.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lab.mops.eval.Main;

public class EvalLoader {

  static final String EVALS_FILE_NAME = "/evals.json";

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static List<Eval> loadEvals() {
    try {
      var evalResource = Main.class.getResourceAsStream(EVALS_FILE_NAME);
      return OBJECT_MAPPER.readValue(evalResource, new TypeReference<>() {});
    } catch (Exception e) {
      throw new RuntimeException("Failed to load evals from " + EVALS_FILE_NAME, e);
    }
  }
}
