package lab.mops.ai.domain.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import nz.geek.jack.libs.ddd.domain.Aggregate;

public class Chat extends Aggregate<ChatId> {

  private final List<Message> messages;

  private Chat(ChatId id, List<Message> messages) {
    super(id);
    this.messages = messages;
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

    var chat = new Chat(ChatId.create(), new ArrayList<>());
    chat.messages.add(Message.userMessage(userPrompt));

    var assistantMessage = Message.assistantMessage();
    chat.messages.add(assistantMessage);

    chat.registerEvent(
        new ChatStartedEvent(
            chat.getId(), chat.getCreatedAt(), userPrompt, assistantMessage.getId()));

    return chat;
  }
}
