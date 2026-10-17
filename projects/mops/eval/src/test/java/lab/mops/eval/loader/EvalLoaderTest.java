package lab.mops.eval.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class EvalLoaderTest extends TestBase {

  @Test
  void loadEvals_validJson_returnsEvalsInOrder() {
    var first = new Eval(randomInt(), randomString(), randomString());
    var second = new Eval(randomInt(), randomString(), randomString());

    var result = EvalLoader.loadEvals(stream("[%s, %s]".formatted(json(first), json(second))));

    assertThat(result).containsExactly(first, second);
  }

  @Test
  void loadEvals_emptyArray_returnsNoEvals() {
    var result = EvalLoader.loadEvals(stream("[]"));

    assertThat(result).isEmpty();
  }

  @Test
  void loadEvals_malformedJson_throws() {
    assertThatThrownBy(() -> EvalLoader.loadEvals(stream("[{" + randomString())))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to load evals from /evals.json");
  }

  @Test
  void loadEvals_unknownField_throws() {
    var input = "[{\"id\": 1, \"%s\": \"%s\"}]".formatted(randomString(), randomString());

    assertThatThrownBy(() -> EvalLoader.loadEvals(stream(input)))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to load evals from /evals.json");
  }

  @Test
  void loadEvals_missingResource_throws() {
    assertThatThrownBy(() -> EvalLoader.loadEvals(null))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to load evals from /evals.json");
  }

  private static String json(Eval eval) {
    return "{\"id\": %d, \"question\": \"%s\", \"answer\": \"%s\"}"
        .formatted(eval.id(), eval.question(), eval.answer());
  }

  private static InputStream stream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }
}
