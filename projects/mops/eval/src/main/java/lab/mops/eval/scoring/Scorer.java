package lab.mops.eval.scoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import java.util.List;
import java.util.Map;
import lab.mops.eval.loader.Eval;

public class Scorer {

  static final Client CLIENT = new Client();

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static final List<String> SCORE_WORDS = List.of("Awful", "Poor", "Good", "Perfect");

  public static Score scoreAnswer(Eval eval, String answer) {
    var schema =
        Schema.builder()
            .type(Type.Known.OBJECT)
            .properties(
                Map.of(
                    "descriptionOfQuality",
                    Schema.builder().type(Type.Known.STRING).build(),
                    "scoreLabel",
                    Schema.builder().type(Type.Known.STRING).build()))
            .required(List.of("descriptionOfQuality", "scoreLabel"))
            .build();

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
            .formatted(formatAnswer(eval, answer), String.join(", ", SCORE_WORDS));

    var config =
        GenerateContentConfig.builder()
            .responseMimeType("application/json")
            .candidateCount(1)
            .responseSchema(schema)
            .build();

    var responseString =
        CLIENT.models.generateContent("gemini-2.0-flash-lite", systemPrompt, config).text();
    var response = parseResponse(responseString);

    var labelIndex = SCORE_WORDS.indexOf(response.scoreLabel());
    if (labelIndex < 0) {
      throw new RuntimeException("Unknown score label: " + response.scoreLabel());
    }

    var scoreValue = (double) labelIndex / (SCORE_WORDS.size() - 1);

    return new Score(response.descriptionOfQuality(), scoreValue);
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

  private static Response parseResponse(String response) {
    try {
      return OBJECT_MAPPER.readValue(response, Response.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
