package lab.mops.eval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Schema;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lab.mops.eval.http.ChatClient;

public class Main {

  static final String EVALS_FILE_NAME = "/evals.json";

  static final Client MOPS_CLIENT = new Client();

  static final ChatClient CHAT_CLIENT = new ChatClient();

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static void main(String[] args) {
    var evals = loadEvals();

    for (var eval : evals) {
      var response = askQuestion(eval.question);
      var score = scoreAnswer(eval, response.answer);

      System.out.printf("Question: [%s]%n", eval.question);
      System.out.printf(
          "Result was: [%s], in: [%s]%n", response.answer, response.duration.toMillis());
      System.out.printf("Score was: %s%n%n", score);
    }
  }

  private static List<Eval> loadEvals() {
    try {
      var evalResource = Main.class.getResourceAsStream(EVALS_FILE_NAME);
      return OBJECT_MAPPER.readValue(evalResource, new TypeReference<>() {});
    } catch (Exception e) {
      throw new RuntimeException("Failed to load evals from " + EVALS_FILE_NAME, e);
    }
  }

  private static AgentResponse askQuestion(String question) {
    var start = Instant.now();

    var answerBody = CHAT_CLIENT.postQuestion(question);

    var duration = Duration.between(start, Instant.now());

    return new AgentResponse(answerBody.message().trim(), duration);
  }

  private static String scoreAnswer(Eval eval, String answer) {
    var schema =
        Schema.fromJson(
            """
        {
          "scores": [
              { "index": 0, "descriptionOfQuality": "", "scoreLabel": "" }
          ]
        }
        """);

    var scoreWords = List.of("Awful", "Poor", "Good", "Perfect");

    var systemPrompt =
        """
      There is an AI assistant that answers questions about budgets in a marketing planning saas application. The questions
      may be asked by marketers or budget owners.

      You are evaluating the quality of an AI assistant's response to several questions. Here are the
      questions, the desired true answers, and the answers given by the AI system:

      <questions>
          %s
      </questions>

      Evaluate each of the assistant's answers separately by replying in specified json format.

      Score only based on whether the assistant's answer is true and answers the question. As long as the
      answer covers the question and is consistent with the truth, it should score as perfect. There is
      no penalty for giving extra on-topic information or advice. Only penalize for missing necessary facts
      or being misleading.

      The descriptionOfQuality should be up to 5 words summarizing to what extent the assistant answer
      is correct and sufficient.

      Based on descriptionOfQuality, the scoreLabel must be one of the following labels, from worst to best: %s
      Do not use any other words for scoreLabel. You may only pick one of those labels.
      """
            .formatted(formatAnswer(eval, answer), String.join(", ", scoreWords));

    var config =
        GenerateContentConfig.builder()
            .responseMimeType("application/json")
            .candidateCount(1)
            .responseSchema(schema)
            .build();

    return MOPS_CLIENT.models.generateContent("gemini-2.0-flash-lite", systemPrompt, config).text();
  }

  private static String formatAnswer(Eval eval, String answer) {
    return """
            <question index="%d">
                <text>%s</text>
                <truth>%s</truth>
                <assistantAnswer>%s</assistantAnswer>
            </question>
        """
        .formatted(eval.id(), eval.question(), eval.answer(), answer);
  }

  record Eval(int id, String question, String answer) {}

  record AgentResponse(String answer, Duration duration) {}
}
