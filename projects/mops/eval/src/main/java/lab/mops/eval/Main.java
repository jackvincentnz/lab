package lab.mops.eval;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import lab.mops.eval.http.ChatClient;
import lab.mops.eval.loader.EvalLoader;
import lab.mops.eval.scoring.Scorer;

public class Main {

  public static void main(String[] args) {
    var evals = EvalLoader.loadEvals();

    var allScores = new ArrayList<Double>();
    var allDurations = new ArrayList<Duration>();

    System.out.println(
        "============================================================================");
    System.out.println();

    for (var eval : evals) {
      var response = timeQuestionResponse(eval.question());
      var score = Scorer.scoreAnswer(eval, response.answer);

      System.out.printf("Question ID: %s%n", eval.id());
      System.out.printf("Question: %s%n", eval.question());
      System.out.printf("Duration: %sms%n", response.duration.toMillis());
      System.out.printf("Score: %s%n", score.score());
      System.out.printf("Justification: %s%n", score.justification());
      System.out.printf("Expected: %s%n", eval.answer());
      System.out.printf("Actual: %n%n%s%n", response.answer);
      System.out.println();
      System.out.println(
          "============================================================================");
      System.out.println();

      allScores.add(score.score());
      allDurations.add(response.duration);
    }

    var avgScore = allScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    var avgDuration = allDurations.stream().mapToDouble(Duration::toMillis).average().orElse(0.0);

    System.out.printf(
        "After %d questions: average score = %.3f, average duration = %.3fms%n",
        allScores.size(), avgScore, avgDuration);
  }

  private static TimedResponse timeQuestionResponse(String question) {
    var start = Instant.now();

    var answerBody = ChatClient.postQuestion(question);

    var duration = Duration.between(start, Instant.now());

    return new TimedResponse(answerBody.message().trim(), duration);
  }

  record TimedResponse(String answer, Duration duration) {}
}
