package lab.mops.eval.http;

import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ChatClient {

  private static final String CHAT_URL = "http://localhost:8080/chats";

  private static final MediaType JSON = MediaType.get("application/json");

  private static final OkHttpClient CLIENT = new OkHttpClient();

  private static final Gson GSON = new Gson();

  public static AnswerBody postQuestion(String question) {
    var json = GSON.toJson(new QuestionBody(question));
    var body = RequestBody.create(json, JSON);
    var request = new Request.Builder().url(CHAT_URL).post(body).build();

    try (var response = CLIENT.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new RuntimeException("Failed to post question");
      }

      return GSON.fromJson(response.body().string(), AnswerBody.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to post question", e);
    }
  }

  record QuestionBody(String prompt) {}

  public record AnswerBody(String message) {}
}
