package lab.mops.ai.domain.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nz.geek.jack.libs.ddd.domain.Aggregate;
import nz.geek.jack.libs.ddd.domain.NotFoundException;

public class Chat extends Aggregate<ChatId> {

  private final List<Message> messages;

  private Chat(ChatId id, List<Message> messages) {
    super(id);
    this.messages = messages;
  }

  public Message addUserMessage(String content) {
    var message = Message.userMessage(content);
    messages.add(message);

    registerEvent(
        new ChatMessageAddedEvent(
            this.id, MessageType.USER, message.getContent().orElseThrow(), message.getTimestamp()));

    return message;
  }

  public void completeMessage(MessageId messageId, String content) {
    var message =
        messages.stream()
            .filter(m -> m.getId().equals(messageId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(messageId));

    message.complete(content);

    registerEvent(new ChatMessageCompletedEvent(this.getId(), message.getId(), content));
  }

  public List<Message> getMessages() {
    return Collections.unmodifiableList(messages);
  }

  public static Chat start(String content) {
    var chat = new Chat(ChatId.create(), new ArrayList<>());
    chat.messages.add(Message.userMessage(content));

    var assistantMessage = Message.assistantMessage();
    chat.messages.add(assistantMessage);

    chat.registerEvent(
        new ChatStartedEvent(chat.getId(), chat.getCreatedAt(), content, assistantMessage.getId()));

    return chat;
  }
}
