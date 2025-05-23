package nz.geek.jack.tools.bdd.github.client.http;

import com.google.gson.Gson;
import java.io.IOException;
import nz.geek.jack.tools.bdd.github.client.model.Snapshot;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SnapshotClient {

  private static final String SNAPSHOT_URL =
      "https://api.github.com/repos/jackvincentnz/lab/dependency-graph/snapshots";

  private static final MediaType JSON = MediaType.get("application/json");

  private static final OkHttpClient CLIENT = new OkHttpClient();

  private static final Gson GSON = new Gson();

  private final String accessToken;

  public SnapshotClient(String accessToken) {
    this.accessToken = accessToken;
  }

  public void postSnapshot(Snapshot snapshot) {
    postSnapshot(GSON.toJson(snapshot));
  }

  private void postSnapshot(String json) {
    var body = RequestBody.create(json, JSON);
    var request =
        new Request.Builder()
            .url(SNAPSHOT_URL)
            .header("Authorization", String.format("Bearer %s", accessToken))
            .post(body)
            .build();

    try (var response = CLIENT.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        failedToPost(null);
      }
      System.out.println(response.body().string());
    } catch (IOException e) {
      failedToPost(e);
    }
  }

  private void failedToPost(Exception e) {
    throw new RuntimeException("Failed to post dependency snapshot to Github", e);
  }
}
