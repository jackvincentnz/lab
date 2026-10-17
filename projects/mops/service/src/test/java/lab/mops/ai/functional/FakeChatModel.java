package lab.mops.ai.functional;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * Answers each prompt with the next scripted reply, so a functional test can drive an assistant
 * turn to completion without calling a real provider.
 */
public class FakeChatModel implements ChatModel {

  private final Queue<String> replies = new ConcurrentLinkedQueue<>();

  public void reply(String content) {
    replies.add(content);
  }

  @Override
  public ChatResponse call(Prompt prompt) {
    var content = replies.poll();
    if (content == null) {
      throw new IllegalStateException("No scripted reply for prompt");
    }

    return ChatResponse.builder()
        .generations(List.of(new Generation(new AssistantMessage(content))))
        .build();
  }
}
