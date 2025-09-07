package lab.mops.ai.domain.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import nz.geek.jack.libs.ddd.domain.Aggregate;

public class Chat extends Aggregate<ChatId> {

  private final List<Message> messages;

  private Chat(ChatId id) {
    super(id);
    this.messages = new ArrayList<>();
  }

  public Message addMessage(String userPrompt) {
    var message = Message.userMessage(userPrompt);
    messages.add(message);

    registerEvent(
        new ChatMessageAddedEvent(
            this.id,
            message.getType(),
            message.getContent().orElseThrow(),
            message.getTimestamp()));

    return message;
  }

  public List<Message> getMessages() {
    return Collections.unmodifiableList(messages);
  }

  public static Chat start(String userPrompt) {
    Objects.requireNonNull(userPrompt, "userPrompt must not be null");

    var chat = new Chat(ChatId.create());
    chat.messages.add(Message.userMessage(userPrompt));
    chat.messages.add(Message.assistantMessage());

    chat.registerEvent(new ChatStartedEvent(chat.getId(), chat.getCreatedAt(), userPrompt));

    return chat;
  }
}
