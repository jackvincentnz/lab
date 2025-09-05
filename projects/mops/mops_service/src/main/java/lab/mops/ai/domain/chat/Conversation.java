package lab.mops.ai.domain.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import nz.geek.jack.libs.ddd.domain.Aggregate;

public class Conversation extends Aggregate<ConversationId> {

  private final List<Message> messages;

  private Conversation(ConversationId id, Instant createdAt) {
    super(id);
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    this.messages = new ArrayList<>();
  }

  public Message addMessage(MessageType type, String content) {
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(content, "content must not be null");

    var message = Message.create(type, content);
    messages.add(message);

    registerEvent(
        new ConversationMessageAddedEvent(
            this.id, message.getType(), message.getContent(), message.getTimestamp()));

    return message;
  }

  public List<Message> getMessages() {
    return Collections.unmodifiableList(messages);
  }

  public static Conversation create() {
    var conversation = new Conversation(ConversationId.create(), Instant.now());

    conversation.registerEvent(new ConversationCreatedEvent(conversation.id));

    return conversation;
  }
}
