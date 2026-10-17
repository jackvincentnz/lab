package lab.mops.eval.scoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class ScorerTest extends TestBase {

  @Test
  void toScore_awfulLabel_scoresZero() {
    var result = Scorer.toScore(responseJson(randomString(), "Awful"));

    assertThat(result.score()).isEqualTo(0.0);
  }

  @Test
  void toScore_poorLabel_scoresOneThird() {
    var result = Scorer.toScore(responseJson(randomString(), "Poor"));

    assertThat(result.score()).isEqualTo(1.0 / 3);
  }

  @Test
  void toScore_goodLabel_scoresTwoThirds() {
    var result = Scorer.toScore(responseJson(randomString(), "Good"));

    assertThat(result.score()).isEqualTo(2.0 / 3);
  }

  @Test
  void toScore_perfectLabel_scoresOne() {
    var result = Scorer.toScore(responseJson(randomString(), "Perfect"));

    assertThat(result.score()).isEqualTo(1.0);
  }

  @Test
  void toScore_withDescription_usesItAsJustification() {
    var description = randomString();

    var result = Scorer.toScore(responseJson(description, "Good"));

    assertThat(result.justification()).isEqualTo(description);
  }

  @Test
  void toScore_unknownLabel_throws() {
    var label = randomString();

    assertThatThrownBy(() -> Scorer.toScore(responseJson(randomString(), label)))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unknown score label: " + label);
  }

  @Test
  void toScore_differentlyCasedLabel_throws() {
    assertThatThrownBy(() -> Scorer.toScore(responseJson(randomString(), "perfect")))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unknown score label: perfect");
  }

  @Test
  void toScore_emptyLabel_throws() {
    assertThatThrownBy(() -> Scorer.toScore(responseJson(randomString(), "")))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unknown score label: ");
  }

  @Test
  void toScore_missingLabel_throws() {
    var response = "{\"descriptionOfQuality\": \"%s\"}".formatted(randomString());

    assertThatThrownBy(() -> Scorer.toScore(response)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void toScore_malformedJson_throws() {
    assertThatThrownBy(() -> Scorer.toScore("{" + randomString()))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void toScore_emptyResponse_throws() {
    assertThatThrownBy(() -> Scorer.toScore("")).isInstanceOf(RuntimeException.class);
  }

  private static String responseJson(String description, String label) {
    return "{\"descriptionOfQuality\": \"%s\", \"scoreLabel\": \"%s\"}"
        .formatted(description, label);
  }
}
